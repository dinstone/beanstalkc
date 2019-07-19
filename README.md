# What
Beanstalkc is a thread-safe client library of the [beanstalkd](https://beanstalkd.github.io), which is based on mina2 and support connection pool.
# How
## Add Maven Dependency:
	<dependency>
	  <groupId>com.dinstone</groupId>
	  <artifactId>beanstalkc</artifactId>
	  <version>2.3.0</version>
	</dependency>

## Example:
```java
// create beanstalkc config,default loading properties from file beanstalkc.properties in classpath
Configuration config = new Configuration();
config.setServiceHost("127.0.0.1");
config.setServicePort(11300);
config.setConnectTimeout(2000);
config.setReadTimeout(3000);
// create job producer and consumer
BeanstalkClientFactory factory = new BeanstalkClientFactory(config);
JobProducer producer = factory.createJobProducer("pctube");
JobConsumer consumer = factory.createJobConsumer("pctube");
// do something
producer.putJob(...);
consumer.reserveJob(...);
// close client and release resources
producer.close();
consumer.close();
```
        