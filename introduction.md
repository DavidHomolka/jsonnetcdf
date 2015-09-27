JsonNetCDF, a Java Library, for loading GRIB/GRIB2/NetCDF data and make queries.

## Objective ##
NetCDF is a format for storing data in multi-dimension array. It is highly compressed and suitable for many cases especially for scientific research. Unfortunately, data in the tables cannot be easily searched and the API is not generally available, so I try to write a small Java library to make queries on it. This library can also read GRIB/GRIB2 data.

## More background information ##
I have tried to write a Java program to insert the data in the GRIB/GRIB2/NetCDF to a SQLite database. I found that the database generated is very large. A 4 MB grib2 file can produce 100MB SQLite database file. Also, if the file is big, it takes quite long time to generate the database. Therefore, I tried to go to another approach, which lets the programmer to query the file directly by the dimensions. It is what this Java class does.

## Requirement ##
As the depended libraries are quite big, I cannot include them in the jar file provided. Please download and include them in the project.

1. NetCDF Java Library from Unidata http://www.unidata.ucar.edu/software/netcdf-java/ . Version 4.1 is used in this project.

2. google-gson from http://code.google.com/p/google-gson/. Version 1.4-Beta is used in this project.

You may choose newer version if available, but I am not sure whether they will still work with the library.

## Limitations ##
The query result should not be too large, otherwise it should take very long time and a lot of memory to process. Sometimes, heap size error will occur. Please use "constraint" to limit the set.

## License ##
This library is released under BSD licence.

## Build the jar file ##
jar cvf jsonnetcdf.jar com/innomarsh/`*`.class