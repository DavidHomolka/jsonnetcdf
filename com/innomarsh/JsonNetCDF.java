/*
Copyright (c) 2010, innoMarsh
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the name of the innoMarsh nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL INNOMARSH BE LIABLE FOR ANY
DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.innomarsh;

import com.google.gson.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import ucar.ma2.Array;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.Range;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;
import ucar.nc2.dataset.NetcdfDataset;

public class JsonNetCDF {
    // the dataset
    private NetcdfDataset gid;
    
    // initialize
    public JsonNetCDF() {
    }

    // initialize and open file
    public JsonNetCDF(String fileName) throws IOException {
        gid = NetcdfDataset.openDataset(fileName);
    }

    // open file
    public void open(String fileName) throws IOException {
        if (gid != null) {
            gid.close();
        }
        gid = NetcdfDataset.openDataset(fileName);
    }

    // close file
    public void close() throws IOException {
        gid.close();
    }

    // query type handler
    public String query(String queryJSON) {
        Gson gson = new Gson();
        Query query = gson.fromJson(queryJSON, Query.class);
        if (query.action.equals("getTable")) {
            // get the table info
            if (gid == null) {
                return "{status: 'error!, reason: 'no file opened'}";
            }
            NetcdfFile gidFile = gid.getReferencedFile();
            return gson.toJson(getTable(query, gid, gidFile));
        } else if (query.action.equals("getData")) {
            // get the data from table
            if (gid == null) {
                return "{status: 'error!, reason: 'no file opened'}";
            }
            NetcdfFile gidFile = gid.getReferencedFile();
            return gson.toJson(getData(query, gid, gidFile));
        } else if (query.action.equals("open")) {
            // open file
            try {
                this.open(query.fileName);
            } catch (IOException ex) {
                return "{status: 'error!', reason: 'fail to open file'}";
            }
            return "{type: 'open', status: 'ok'}";
        } else if (query.action.equals("close")) {
            // close file
            try {
                gid.close();
            } catch (IOException ex) {
                return "{status: 'error!', reason: 'fail to close file'}";
            }
            return "{type: 'close', status: 'ok'}";
        }
        return "";
    }

    private HashMap getTable(Query query, NetcdfDataset gid, NetcdfFile gidFile) {
        List<Variable> variables = null;
        HashMap response = new HashMap();
        ArrayList variablesArray = new ArrayList();
        response.put("list", variablesArray);
        response.put("type", "list");

        // if a table name is given, check whether it exists. If so, get the info of that table
        if ((query.table != null) && (!query.table.equals(""))) {
            variables = Arrays.asList(gidFile.findVariable(query.table));
            if (variables.get(0) == null) {
                response.put("status", "Error!");
                response.put("reason", "no such table");
                return response;
            }
        } else {
            // else, get the info of all tables
            variables = gidFile.getVariables();
        }

        // get the attributes
        for (int i = 0; i < variables.size(); i++) {
            Variable variable = variables.get(i);
            HashMap variableHash = new HashMap();
            variableHash.put("description", variable.getDescription());
            variableHash.put("attributes", variable.getAttributes().toString());
            variableHash.put("dataType", variable.getDataType().toString());
            variableHash.put("dimensions", variable.getDimensions().toString());
            variableHash.put("dimensionsString", variable.getDimensionsString());
            variableHash.put("elementSize", variable.getElementSize());
            variableHash.put("name", variable.getName());
            variableHash.put("nameEscaped", variable.getNameEscaped());
            variableHash.put("rank", variable.getRank());
            variableHash.put("shapeAsSection", variable.getShapeAsSection().toString());
            variableHash.put("shortName", variable.getShortName());
            variableHash.put("size", variable.getSize());
            variableHash.put("unitString", variable.getUnitsString());
            variableHash.put("isCoordinateVariable", variable.isCoordinateVariable());
            variableHash.put("isMetadata", variable.isMetadata());
            variableHash.put("isScalar", variable.isScalar());
            ArrayList dimensionArray = new ArrayList();
            // get the dimension data
            for (int j = 0; j < variable.getDimensions().size(); j++) {
                HashMap dimensionHash = new HashMap();
                Dimension dimension = variable.getDimension(j);
                dimensionHash.put("length", dimension.getLength());
                dimensionHash.put("name", dimension.getName());
                if ((gidFile.findVariable(dimension.getName()) != null)) {
                    if (gidFile.findVariable(dimension.getName()).findAttribute("_CoordinateAxisType") != null) {
                        dimensionHash.put("dimensionType", gidFile.findVariable(dimension.getName()).findAttribute("_CoordinateAxisType").getStringValue());
                    }
                    dimensionHash.put("unitString", gidFile.findVariable(dimension.getName()).getUnitsString());
                } else {
                    dimensionHash.put("unitString", "");
                }
                dimensionArray.add(dimensionHash);
            }
            variableHash.put("dimensionArray", dimensionArray);
            variablesArray.add(variableHash);
        }
        return response;
    }

    private HashMap getData(Query query, NetcdfDataset gid, NetcdfFile gidFile) {
        HashMap response = new HashMap();
        response.put("type", "data");
        // table name must be given
        if ((query.table == null) || (query.table.equals(""))) {
            response.put("status", "Error!");
            response.put("reason", "no table name");
            return response;
        }
        response.put("table", query.table);
        Array dataArray;
        int dimensionTotal = 1;
        ArrayList dimensionsSequence = new ArrayList();
        HashMap dimensionsList = new HashMap();
        try {
            List<Dimension> dimensions;
            // check whether the table exists
            try {
                dimensions = gid.findVariable(query.table).getDimensions();
            } catch (Exception e) {
                response.put("status", "Error!");
                response.put("reason", "no such table");
                return response;
            }
            ArrayList ranges = new ArrayList();
            for (int i = 0; i < dimensions.size(); i++) {
                String dimensionType = gidFile.findVariable(dimensions.get(i).getName()).findAttribute("_CoordinateAxisType").getStringValue();

                Array dimensionValues = gidFile.findVariable(dimensions.get(i).getName()).read();
                ArrayList<Float> values = new ArrayList();
                for (int j = 0; j < dimensionValues.getSize(); j++) {
                    values.add(dimensionValues.getFloat(j));
                }
                int min = 0;
                int max = values.size() - 1;
                //check constraint, they are in 'AND' relationships
                if (query.constraint != null) {
                    for (int j = 0; j < query.constraint.length; j++) {
                        // max cannot be smaller than min
                        if ((query.constraint[j].min != (float) -999999999) && (query.constraint[j].max != (float) -999999999) && (query.constraint[j].max < query.constraint[j].min)) {
                            response.put("status", "Error!");
                            response.put("reason", "max cannot be smaller than min!");
                            return response;
                        }

                        if (query.constraint[j].name.equals(dimensionType)) {
                            boolean hasSetMin = false;
                            boolean hasSetMax = false;
                            if (values.get(values.size() - 1) > values.get(0)) {
                                for (int k = 0; k < values.size(); k++) {
                                    if ((query.constraint[j].min != (float) -999999999) && (values.get(k) < query.constraint[j].min)) {
                                        min = k + 1;
                                    }
                                    if ((!hasSetMax) && (query.constraint[j].max != (float) -999999999) && (values.get(k) > query.constraint[j].max)) {
                                        max = k - 1;
                                        hasSetMax = true;
                                    }
                                }
                            } else {
                                for (int k = (values.size() - 1); k >= 0; k--) {
                                    if ((!hasSetMin) && (query.constraint[j].min != (float) -999999999) && (values.get(k) < query.constraint[j].min)) {
                                        min = k - 1;
                                        hasSetMin = true;
                                    }
                                    if ((query.constraint[j].max != (float) -999999999) && (values.get(k) > query.constraint[j].max)) {
                                        max = k + 1;
                                    }
                                }
                            }
                            break;
                        }
                    }
                }
                ArrayList<Float> newValues = new ArrayList();
                for (int j = min; j < (max + 1); j++) {
                    newValues.add(values.get(j));
                }
                dimensionsSequence.add(dimensionType);
                dimensionsList.put(dimensionType, newValues);
                // range add to the search
                try {
                    ranges.add(new Range(min, max));
                } catch (InvalidRangeException ex) {
                    response.put("status", "Error!");
                    response.put("reason", "out of range");
                    return response;
                }
            }
            // calculate for next step
            for (int i = 0; i < dimensionsSequence.size(); i++) {
                ArrayList<Float> values = (ArrayList<Float>) dimensionsList.get(dimensionsSequence.get(i));
                dimensionTotal *= values.size();
            }
            // read the table
            try {
                dataArray = gid.findVariable(query.table).read(ranges);
            } catch (InvalidRangeException ex) {
                response.put("status", "Error!");
                response.put("reason", "no revelant table");
                return response;
            }
        } catch (IOException ex) {
            response.put("status", "Error!");
            return response;
        }
        ArrayList dataList = new ArrayList();
        response.put("data", dataList);
        float[] data = (float[]) dataArray.copyTo1DJavaArray();
        // merge the data with dimension data
        for (int i = 0; i < data.length; i++) {
            HashMap dataMap = new HashMap();
            int previousDimensionTotal = dimensionTotal;
            for (int j = 0; j < dimensionsSequence.size(); j++) {
                ArrayList<Float> dimension = ((ArrayList<Float>) dimensionsList.get(dimensionsSequence.get(j)));
                previousDimensionTotal /= dimension.size();
                int arrayNumber = (i / previousDimensionTotal) % dimension.size();
                Float dimensionValue = dimension.get(arrayNumber);
                dataMap.put(dimensionsSequence.get(j), dimensionValue);
            }
            dataMap.put("value", data[i]);
            dataList.add(dataMap);
        }
        return response;
    }
}

// internal class for the query
class Query {
    public String action;
    public String table;
    public Constraint[] constraint;
    String fileName;
}

// internal class for the constraint
class Constraint {
    public String name;
    public float min = (float) -999999999;
    public float max = (float) -999999999;
}