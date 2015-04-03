This will eventually include a lot more information.

## Maven 2 ##
The steps on adding this project into your maven 2 pom.xml file:

Define the Sakai maven repository in the repositories section of your pom.xml (this is a top level section and not nested in other sections normally):
```
    <repositories>
        <!-- sakai maven 2 repo - copy from here -->
        <repository>
            <id>sakai-maven</id>
            <name>Sakai Maven Repo</name>
            <layout>default</layout>
            <url>http://source.sakaiproject.org/maven2</url>
            <snapshots>
                <enabled>false</enabled>
            </snapshots>
        </repository>
        <!-- copy down to here -->
...
        <!-- the default maven 2 repo as an example -->
        <repository>
            <id>default</id>
            <name>Maven Repository Switchboard</name>
            <layout>default</layout>
            <url>http://repo1.maven.org/maven2</url>
            <snapshots>
                <enabled>false</enabled>
            </snapshots>
        </repository>
...
    </repositories>
```

The maven dependency to add to your dependencies section of your pom.xml:
```
        <dependencies>
...
            <!-- Reflection Utils -->
            <dependency>
                <groupId>org.azeckoski</groupId>
                <artifactId>reflectutils</artifactId>
                <version>0.9.15</version>
                <scope>compile</scope>
            </dependency>
        </dependencies>
```

Now this project is added as a dependency to your maven 2 project.