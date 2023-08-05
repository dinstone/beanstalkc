# What

Beanstalkc is a thread-safe client library of the [beanstalkd](https://beanstalkd.github.io), which support connection pool.

# How

## Add Maven Dependency:

```xml
<dependency>
  <groupId>com.dinstone.beanstalkc</groupId>
  <artifactId>beanstalkc-netty</artifactId>
  <version>2.4.0</version>
</dependency>
```

or

```xml
<dependency>
  <groupId>com.dinstone.beanstalkc</groupId>
  <artifactId>beanstalkc-mina</artifactId>
  <version>2.4.0</version>
</dependency>
```

## Example:

```java
public static void main(String[] args) {
    // set beanstalkd service host and port and other connetion config,
    // then create job producer or consumer by this config.
    Configuration config = new Configuration();
    config.setServiceHost("192.168.1.120");
    config.setServicePort(11300);
    config.setConnectTimeout(2000);
    config.setReadTimeout(3000);
    BeanstalkClientFactory factory = new BeanstalkClientFactory(config);
    
    // create an producer instance 
    JobProducer producer = factory.createJobProducer("pctube");
    
    // publish delay job to beanstalkd
    producer.putJob(1, 1, 5000, "dddd".getBytes());

    // create an consumer instance  
    JobConsumer consumer = factory.createJobConsumer("pctube");

    // peek delay job from beanstalkd
    Job job = consumer.reserveJob(1);
    
    // do someting
    System.out.println("id = " + job.getId() + " ; data = " + new String(job.getData()));
    
    // job execute success, then delete it
    consumer.deleteJob(job.getId());
    
    // finnally close client and release resources
    producer.close();
    consumer.close();
}
```
        