## Introduction ##
Just use it as an command-line interface to the library. One-line input returns one-line output.

Reminders: include all the jar files needed.

```
package jsonnetcdfconsole;

import com.innomarsh.JsonNetCDF;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {
    public static void main(String[] args) {
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader inputReader = new BufferedReader(input);
        String inputLine = "";
        JsonNetCDF jsonNetCDF = new JsonNetCDF();
        while(true){
            try {
                inputLine = inputReader.readLine();
                if (inputLine.equals("exit")){
                    break;
                }
                System.out.println(jsonNetCDF.query(inputLine));
            } catch (IOException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            } catch (Exception ex){
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
```

## Python 2.6 example that uses this command line interface ##
```
#!/usr/bin/env python
from subprocess import Popen, PIPE, STDOUT
import json
import sys, traceback
try:
    #path of jar
    program = Popen('java -jar JsonNetCDF-Console.jar', shell=True, stderr=PIPE, stdout=PIPE, stdin=PIPE)
    
    #open file
    program.stdin.write('{action: "open", fileName: "gfs_4_20100420_0600_180.grb2"}\n')
    program.stdin.flush()
    line = program.stdout.readline()
    print line
    
    #get table
    program.stdin.write('{action: "getTable", table: "Temperature"}\n')
    program.stdin.flush()
    line = program.stdout.readline()
    decoded = json.loads(line)
    print decoded["list"][0]["shortName"]
    print decoded["list"][0]["unitString"]
    
    #get data
    program.stdin.write('{action: "getData", table: "Temperature", constraint: [{name: "Pressure", min: 100000, max: 100000}, {name: "Lat", max: 30, min: 10}, {name: "Lon", max: 150, min: 100}], separateColumn: true}\n')
    program.stdin.flush()
    line = program.stdout.readline()
    decoded = json.loads(line)
    print decoded["data"]["Lat"]
    print decoded["data"]['value']
    for i in decoded["data"]["value"]:
        print i
    
    #get data
    program.stdin.write('{action: "getData", table: "Temperature", constraint: [{name: "Pressure", min: 100000, max: 100000}, {name: "Lat", max: 30, min: 10}, {name: "Lon", max: 150, min: 100}]}\n')
    program.stdin.flush()
    line = program.stdout.readline()
    decoded = json.loads(line)
    print decoded["data"][0]
    print decoded["data"][0]['value']
    for i in decoded["data"]:
        print i['value']
except Exception as e:
    print "Unexpected error!"
    print traceback.format_exception(*sys.exc_info())
finally:
    #exit
    program.stdin.write('exit\n')
    program.stdin.flush()
    program.kill()

```