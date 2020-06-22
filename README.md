# Go-Game

Go is a two-player abstract strategy game, in which the objective is to surround more territory than the opponent. The playing pieces are called “stones”. One player uses the white stone and the other uses black. The players take turns placing the stones on the vacant intersections (“points”) of a board. Once the stones placed on the board, they may not be moved, but stones are removed from the board if "captured". Capture happens when a stone or group of stones is surrounded by opposing stones on all orthogonally adjacent points. The game proceeds until neither player wishes to make another move or say, there is no profitable move left for the current player to play. When a game concludes, the winner is determined by counting each player’s surrounded territory along with captured stones. The standard GO board has 19*19 grid of lines, containing 361 points. This interesting game had been developed with the security requirements that were taught during the course. 

We used Client-server architecture as our software design pattern for our game. A Client-Server Architecture is a distributed application structure that splits the tasks separately. The Architecture contains server, client and database modules. In a network, one or many clients request service to the server and the server receives and responds to the request from the clients. 

JSF is very convenient as it is based on the Model View Controller architecture, where all the components are readily available to develop a web application at developer’s dispose. The model encapsulates the data, View is responsible for the UI logic of the application and the Controller is responsible for processing the requests from the users by controlling the model data and the view. In our project, the architecture we used is modeled using JSF  MVC Framework so that different aspects of the application were kept separately. Our game is developed in a Java-based application. 
 
The project made use of the MYSQL database management system to store and retrieve the data that was used and forms the bottom layer in our architecture. Once we figured out the flow between the database object and the client, we utilized the Service layer to operate on the data. In this way, we were able to follow the Dependency Injection mechanism. The next step was to execute the functionality specific to the request that was made by the client which was accomplished by the AppController component. The server-side validations were also performed in this layer. The presentation layer that we created using CSS and Javascript, provides a rich graphical user interface. 
Since our focus was more on the security aspect, we used the Spring security framework for the best. 

This framework provided us the support for authentication and authorization security features. Authentication is identifying the individual who needs access to the entity. And Authorization is the process to allow access to that individual. One of the main advantages of using this framework was that it provided protection against Cross Site Scripting attacks. This security framework was integrated with the JSF MVC framework in our project. 

Programming Language: JAVA 
Database: MYSQL Workbench 8.0 
Maven: Build automation tool for our application. 
Server: Apache Tomcat 9.0. 

Type about each java file: 
 
CreateAccountBean: This component handles all the data from the user, like username, password, etc., required to create her account. 

GameBean: This component handles the data like move, capture piece, win, loss, etc., required to run the game. 

LandingPageBean: This component handles sessions at the start of the new game and when the player joins the game. It also manages the log out. 

LoginBean: This component has the regular expression to check for the allowed characters in passwords and login name. 

AuthenticationService.java: This component verifies the credentials of the users. It checks on the number of attempts a user can make, checks if the player has not left any of these mandatory things blank or empty. If credentials are wrong for a certain number of attempts, it locks out the user. 

AutherizationFilter.java: This component makes a request to the servlet to handle views and related classes and creates sessions. 

DataDriver.java: This component helps to access the database and perform the CRUD (create, read, update, delete) operations. 

GameInstance.java: Sets and gets every detail of the player like the number of passes, scores, etc. 

ContextHelper.java: Gets the current player owning the session. Also, terminates it when the player logs out.  

GoLogic.java: This component handles the main interactivity of the game like capturing pieces, setting colors of the board and changing the gameState array which contains the color of the board which player has placed. It handles the logic while the player is capturing the piece. The invalid capture is prompted as an error to the user. 

GameLog.java: Maintains the log of which player is playing which move. 

Userprofile.java: A POJO class which contains all the statistics of the player like win and loss, username, hashed password, etc. 

XHTML Pages: These add up the view into the bean classes. These are the view components of the MVC. 

Web.xml:  A deployment descriptor file for servlet-based java web application declaring which URL’s the servlet should handle. 

Pom.xml: It is the fundamental unit of work in Maven that contains information about the project and configuration details used by Maven to build the project. 
 
