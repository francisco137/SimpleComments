# Comments - The Scala - Play Framework Application

## The Application Goal 

The aim of the application 'Comments' is to create dockerized REST API application
for Scala Play2 Framework where in/out communication is through json objects. 

## Installing and running the application

To install, test and run application first download project, copy/unpack its files
to separated directory, go to the directory and run the main bash script with option *--init*:

`./comments.sh --init`

This creates docker image which consists of the following main parts: debian-buster,
openjdk, sbt, Scala-Play2 Framework (with Slick), PostgreSQL. Therefore the first 
time a lot of files will be download from the internet to build the docker image 
(about 1.2GB). After creation of the application image proper initialization 
process starts inside docker image. This part in turn will download next 0.5GB of data 
(jar library files). This stage consists of Postgres database instalation,
importing some set of data (lists of US hospitals publicly available), compilation of
the Scala source files and finally performing some basic tests.

Next step is running application using the same script with the option *--run*:

`./comments.sh --run`

The application works as a deamon and is accesible at localhost only on the port 9000.

To stop the application run the script with the option *--stop*:

`./comments.sh --stop`

The application consists of 1 docker image named:

1. *francisco_comments* (1.2GB)

and 3 docker volumes:

1. *francisco_comm* - to keep application files (10MB)
2. *francisco_postgres* - to keep database data (80MB)
3. *francisco_root* - to keep scala and java libraries (360MB)

To recompile the sources the volumes must be removed respectively. For this you can add 
one or more arguments of the four: *with_post*, *with_root*, *with_comm* and *with_home* 
to the *--init* parameter like:

`./comments.sh --init with_comm`

to remove *francisco_comments* volume. The volumnes are for:

- *with_comm* - is for the application files
- *with_home* - is for the database user data
- *with_post* - is for the directory of Postgres cluster
- *with_root* - is for downloaded Scala and Java libraries


## Instruction 

This is RESTFUL application with a Json interface. In the following examples
we just use syntax related to the http client program named **curl**.

The application has the following end point:

####1. To get hello:

`curl http://localhost:9000`

The respond is just short Json-form help for user. Other end points are grouped into
one records operation and many records operations: 

####2. One record end points:

a) Inserting new comment:

`curl -X POST -H "Content-Type: application/json" -d "{\"content\":\"this is my new comment\"}" http://localhost:9000/comments`

Response: `{"id":4776}`

b) Updating existing comment with id (4776):

`curl -X PUT -H "Content-Type: application/json" -d "{\"content\":\"this is my updated comment\"}" http://localhost:9000/comments/4776`

Response: `{"result":true}` if succeded or `{"result":false,"reason": "Item not found"}` otherwise.

c) Selecting existing comment with id (4776):
`curl -X GET http://localhost:9000/comments/{id}`

Response: `[{"id":4775,"content":"this is my updated comment"}]`

- note 1: This is 1 element array
- note 2: It can be added *prefix* parameter as follows:

`curl -X GET http://localhost:9000/comments/4775?prefix=francisco`

to get the following answer:

Response: `[{"id":4775,"content":"franciscothis is my updated comment"}]`

d) Deleting existing comment with id (4776):

`curl -X DELETE http://localhost:9000/comments/4807`

Response: `{"result":true}` if succeded or `{"result":false,"reason": "Item not found"}` otherwise.


####3. Multi records operations:

Multi records operation deal exclusively with querying comments database, i.e.
only GET request are used. Responses are as JSON array of JObject of the form:

```
[
    {"id":234,"content":"this is the content 1"},
    {"id":235,"content":"this is the content 2"},
    ...
    {"id":235456,"content":"this is the content 23456"}
]
```

In principle we have only one end point here, namely:

`curl -X GET "http://localhost:9000/comments?prefix=francisco-&filter=new york&sort=S"`

We present here all possible (and *optional*) arguments which can be used together with the 
request.

- argument `prefix` was already explained where one record select request has been described,

- argument `sort` has 3 posible values: `L`, `S` and `D`, where

    - `L` is sorting by comment contents which are treated as lists of words
     (lexigraphically) and these lists are compared. Please note that
     all characters other than lexigraphical ones are neglected in the response.
     
    - `S` - is normal sorting by contents which are treated as strings. Uppercase and 
    lowercase characters are distinguish.
    
    - `D` - is a special sorting similar to the `L` one, but before comparison
    each of the lists are sorted inside by theirs inside words. This way 
    comment is treated as a collection of words and as a result the previously
    distant comments could appear quite close on the final list and vice-verse.
    In this case not the original comments are presented but their transformed version.
    
- argument `filter` is used to get only subset of all comments. During filtering
only the following characters are used: [\w ] i.e. spaces, 0-9, a-z and  A-Z. 
For instance when we write something like `filter=new york` the application will 
change it to the SQL phrase: `WHERE content ILIKE '%new york%'`. 
The `ILIKE` stands for `LIKE` case insensitive. 

There is an additional functionality related to the filtering. The internal 
parser allows to build more sophisticated filters. To the basic characters ([\w ])
user can add three operational ones (plus rounded brackets): `*`, `,`, `-`, `(`, `)`,
where:

- `*` is AND logical operation
- `,` is OR  logical operation
- `-` is NOT logical operation

and `()` are round brackets.

Few examples:

- `new york*(florida,texas)` == `content ILIKE '%new york%' AND (content ILIKE '%florida%' OR content ILIKE '%texas%')`

- `new york*-florida` == `content ILIKE '%new york%' AND NOT(content ILIKE '%florida%')`

- `new york,florida` == `content ILIKE '%new york%' OR content ILIKE '%florida%'`

- `new york,-florida` == `content ILIKE '%new york%' OR NOT(content ILIKE '%florida%')`

and so on.

