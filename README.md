com.dinstone.beanstalkc
=======================

Beanstalkc is a beanstalk client library, which is based on mina2.
-----------------------------------------------------------------------------
Example:

		// create beanstalkc config,default loading properties from file beanstalkc.properties in classpath
		Configuration config = new Configuration();
		config.setServiceHost("127.0.0.1");
		config.setServicePort(11300);
		
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

        