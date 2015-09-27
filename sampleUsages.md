
```
//import the library
import com.innomarsh.JsonNetCDF;

// create the instance
JsonNetCDF jsonNetCDF = new JsonNetCDF();

// open gfs.t06z.mastergrb2f12
// it must be done before reading the data
String result = jsonNetCDF.query("{action: 'open', fileName: 'gfs.t06z.mastergrb2f12'}");

// get the tables and meta data in the file
result = jsonNetCDF.query("{action: 'getTable'}");

// get the meta data of table 'Temperature'
result = jsonNetCDF.query("{action: 'getTable', table: 'Temperature'}");

// get the data in the table  'Temperature'
// Do not do this when the original data set is large! Otherwise it will take very long time and a lot of memory to process
// "constraint" is needed to limit the output
result = jsonNetCDF.query("{action: 'getData', table: 'Temperature'}");

// get the data in the table  'Temperature' with latitude (the name depends on the file) >=22
// the constraints are the dimensions which can be looked up with the action 'getTable'
result = jsonNetCDF.query("{action: 'getData', table: 'Temperature', constraint: [{name: 'Lat', min: 22}]}");

// get the data in the table  'Temperature' with latitude >=22 and <=25
result = jsonNetCDF.query("{action: 'getData', table: 'Temperature', constraint: [{name: 'Lat', min: 22, max: 25}]}");

// get the data in the table  'Temperature' with latitude >=22 and <=25 and longitude >=110 and <=115
result = jsonNetCDF.query("{action: 'getData', table: 'Temperature', constraint: [{name: 'Lat', min: 22, max: 25}, {name: 'Lon', min: 110, max: 115}]}");

// get the data in the table  'Temperature' with latitude =22 and longitude =110
result = jsonNetCDF.query("{action: 'getData', table: 'Temperature', constraint: [{name: 'Lat', min: 22, max: 22}, {name: 'Lon', min: 110, max: 110}]}");

// get the data in the table  'Temperature' with latitude =22 and longitude =110
// with different data representation (array of point data changes to dimension data array)
result = jsonNetCDF.query("{action: 'getData', table: 'Temperature', constraint: [{name: 'Lat', min: 22, max: 22}, {name: 'Lon', min: 110, max: 110}], separateColumn: true}");

// remark: the minimum pressure is 50000Pa, which will load the value of pressure larger than 500hPa
// It is different from the pressure layer in nature where lower value means higher level
result = jsonNetCDF.query("{action: 'getData', table: 'Temperature', " +
    "constraint: [{name: 'Lat', min: 10, max:40},{name: 'Lon', min: 100, max: 140}, {name: 'Time', max: 0}, {name: 'Pressure',min: 50000}]}");

// remark: all statement of 'getData' can be replaced by 'getRange' for getting the range to read.
// The range can be used read() function in the NetCDF-Java library.
result = jsonNetCDF.query("{action: 'getRange', table: 'Temperature', constraint: [{name: 'Lat', min: 22, max: 22}, {name: 'Lon', min: 110, max: 110}], separateColumn: true}");

// close the file
result = jsonNetCDF.query("{action: 'close'}");
```