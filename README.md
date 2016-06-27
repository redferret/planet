## Planet
A simulation that simulates geological processes as well as hydrological, biological, and other complex systems that can exist on a planet.

To view some images of what the engine has produced visit this site: http://cianferret.deviantart.com/

### Please Read the Wiki

## Building
Simply run Ant on the root directory where the build XML file is located. Newer versions of the project will eventually move to Maven

There is currently a bug in the project's properties file where the JDK home is not set, if you get this error when building:
<p>
<code>
C:\Users\rich\git\planet\nbproject\build-impl.xml:86: The J2SE Platform is not correctly set up.
 Your active platform is: JDK_1.8, but the corresponding property "platforms.JDK_1.8.home" is not found in the project's
 properties files.
 Either open the project in the IDE and setup the Platform with the same name or add it manually.
 For example like this:
     ant -Duser.properties.file=<path_to_property_file> jar (where you put the property "platforms.JDK_1.8.home" in a .p
roperties file)
  or ant -Dplatforms.JDK_1.8.home=<path_to_JDK_home> jar (where no properties file is used)
</code></p>

Simply add, to the project.properties file:<br/>
platforms.JDK_1.8.home=C:\\\Program Files\\\Java\\\jdk1.8.0_73\\\
<br/>
Or where ever your JDK home is located.<br/>
Double \\\ is required otherwise it won't parse correctly.

The libs folder at the root directory contains all the libraries the project uses.

## IDE
The current version is using NetBeans 8.0.2 hence the nbproject directory.

## Project Status
The project needs LWJGL programmers to give a good UI to the simulation.

## Contributors
If you wish to contribute to the project, I'm looking for those skilled in 3D graphics. The simulation will be using LWJGL.
