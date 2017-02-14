## License validation sample - scenario

* API Manager is deployed at a customer's location along with a set of services. Treated as a "blackbox" by the customer
* Customer want to access API_1, API_2, and API_5 (from API_{1..10} that's deployed and baked in to the "blackbox") and request with a purchase order
* Create license key based on API_1, API_2 and API_5 and send to customer
* Customer put the license key to license validator app and click 'Activate'
* Microservice will,
    * Validate requested APIs (from data that's stored locally)
    * Subscribe to requested APIs
    * Generate consumer key + secret with an access toke (with a large expiration time)
    * Send access token back

## Database setup

Create a database in MySQL.
```
CREATE DATABASE scg;

GRANT ALL ON sgc.* TO scg@localhost IDENTIFIED BY 'scg';

FLUSH PRIVILEGES;
```
Then create a table
```
CREATE TABLE `apis` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL,
  `apikey` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=latin1;

INSERT INTO `apis` VALUES (1,'API_1','c40138bf-1a82-4644-8ad8-23311d78ea3f'),(2,'API_2','d520585f-139b-4929-af8f-d7f2b0de3c1f'),(3,'API_5','d25890cb-21a7-4c08-bbac-d18acb8f5808');
```
Check to see values are there
```
mysql> select * from apis;
+----+-------+--------------------------------------+
| id | name  | apikey                               |
+----+-------+--------------------------------------+
|  1 | API_1 | c40138bf-1a82-4644-8ad8-23311d78ea3f |
|  2 | API_2 | d520585f-139b-4929-af8f-d7f2b0de3c1f |
|  3 | API_5 | d25890cb-21a7-4c08-bbac-d18acb8f5808 |
+----+-------+--------------------------------------+
3 rows in set (0.00 sec)
```

## Running the microservice

Run the microservice inside activate-microservice folder. You can load the project into the IDE and run it or use the command line.
```
$ mvn clean install
$ java -jar target/activate-1.0-SNAPSHOT.jar
2017-02-13 15:42:12 INFO  MicroservicesRegistry:76 - Added microservice: org.example.service.MyService@4b9af9a9
2017-02-13 15:42:12 INFO  NettyListener:56 - Starting Netty Http Transport Listener
2017-02-13 15:42:12 INFO  NettyListener:80 - Netty Listener starting on port 8080
2017-02-13 15:42:12 INFO  MicroservicesRunner:122 - Microservices server started in 339ms
```
As you can see, now microservice should be running on port 8080

## Run client application

Go to clientapp folder and use go run to run the sample client.
```
$ go run license-validation-client.go
2017/02/13 15:43:47 App running on http://localhost:8181
```
Client application is now accessible from http://localhost:8181. Open up a browser and go there.

## Run API Manager 2.1.0
Download and run API Manager 2.1.0

## Running/testing the sample

Format of the license key is the base64 encoded version of APIs customer paid for. So, if customer wants to access API_1, API_2 and API_5 corresponding licenese key would be,

```
Base64(c40138bf-1a82-4644-8ad8-23311d78ea3f,d520585f-139b-4929-af8f-d7f2b0de3c1f,d25890cb-21a7-4c08-bbac-d18acb8f5808)
```
Please note that keys are separated by a comma.

For the above scenario input string would be,
```
YzQwMTM4YmYtMWE4Mi00NjQ0LThhZDgtMjMzMTFkNzhlYTNmLGQ1MjA1ODVmLTEzOWItNDkyOS1hZjhmLWQ3ZjJiMGRlM2MxZixkMjU4OTBjYi0yMWE3LTRjMDgtYmJhYy1kMThhY2I4ZjU4MDg=
```
Copy and pase above string to license key input box in webapp and click activate.

You should see an access token. Now this access token can be used to access API_1, API_2 and API_5.

![access token](https://raw.githubusercontent.com/chintana/license-validation-sample/master/access-token.png)
