# About
Caramel is a 2D Game Engine that is solely built on the Java Programming language. Why did I choose this language?
Firstly, it is still one of the most widely used languages worldwide. Secondly, I feel much better coding in
Java than a faster but more complicated language such as C or C++.

# Documentation
Since Caramel is a Java project, I have included a portable version of JDK 1.8 inside the `jdk/` folder. Why JDK is
needed you ask? Well, JDK is needed to compile Java script classes (not JavaScript) into components for each
GameObject. That is right! You can use Java as a scripting language to program the logic behind your game! Simply run
`Caramel.exe` with the `jdk/` and `launch4j.xml` beside it and you will see the engine open!
![See Preview](https://img001.prntscr.com/file/img001/wxTsCkIWTFmO6FFEottVkg.png "Caramel")

#### Creating a GameObject
Right click the Hierarchy Panel and select the GameObject you want to create.
![See Preview](https://img001.prntscr.com/file/img001/OOpaqmbrTACBjCwET6kpjw.png "Create GameObject")

#### Editing Components
Select the GameObject you want to edit and go to the Inspector Panel to add a Component.
From here, you can manipulate the Components of the GameObject and change whatever you want.
![See Preview](https://img001.prntscr.com/file/img001/GAsupg_oQymOuoGwB9OkcQ.png "Editing Component")

# Scripting
To create a Script for your GameObject, scroll down until you see Create Script, and enter a script name to your preference
and click on Create.
![See Preview](https://img001.prntscr.com/file/img001/prLHhhItR5eSZp1inKwoMQ.png "Scripting")

This is what a Script looks like. It is similar to Unity Scripting, so you might get the hang of it pretty quickly.
![See Preview](https://img001.prntscr.com/file/img001/hZslpVwKTaycAE5EBrRMHw.png "Scripting")

If you want intellisense for your scripts, you can add the API dependency using Maven:
```
<repository>
    <id>caramel-repo</id>
    <url>https://repo.destial.xyz/repository/maven-snapshots/</url>
</repository>

<dependency>
    <groupId>xyz.destiall.gameengine</groupId>
    <artifactId>api</artifactId>
    <version>1.0-SNAPSHOT</version>
    <scope>provided</scope>
</dependency>
```

#### Java Docs
You can find the Java Docs for the API in `docs/`.
