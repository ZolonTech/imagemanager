package com.cfx.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.minio.MinioClient;


public class ServiceConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(ServiceConfig.class);
        
    private static final ServiceConfig _instance = new ServiceConfig();
    private Properties macawProperties = null;
    
    static {
        _instance.start();
    }
            
    private ServiceConfig() {
    }
    
    public static ServiceConfig getInstance() {
        return _instance;
    }
    
    public String getUserid() {
        return getProperty("io.macaw.rest.user", "admin@macaw.io");
    }

    public String getPassword() {
    	return getProperty("io.macaw.rest.password", "abcd123$");
    }
    
    public String getMinioServer() {
        return getProperty(Constants.PROPERTYNAME_OBJECTSTORE_HOST, "");
    }
    
    public String getMinioPort() {
        return getProperty(Constants.PROPERTYNAME_OBJECTSTORE_PORT, "");
    }
    
    public String getMinioAccessKey() {
        return getProperty(Constants.PROPERTYNAME_OBJECTSTORE_KEY_ACCESS, "");
    }
    
    public String getMinioSecretKey() {
        return getProperty(Constants.PROPERTYNAME_OBJECTSTORE_KEY_SECRET, "");
    }
    
    public String getApiGatewayHost() {
        return getProperty("api.gateway.host", "");
    }
    
    public String getApiGatewayPort() {
        return getProperty("api.gateway.port", "");
    }
    
    public String getApiGatewayProtocol() {
        return getProperty("api.gateway.protocol", "");
    }
    
    public String getServiceName() {
        return getProperty("image.manager.service.name", "image-manager");
    }
    
    public String getServiceNamespace() {
        return getProperty("image.manager.service.name.space", "io.zolontech.services");
    }
    
    public String getServiceVersion() {
        return getProperty("image.manager.service.version", "*");
    }

    public void start()  {
        this.macawProperties = getMacawProperties();
    }
    
    public void stop() {
    }
    
    public Properties getMacawProperties() {
        if (macawProperties == null || macawProperties.isEmpty()) {
                try {
                    macawProperties = loadMacawProperties();
                } catch (Exception e) {
                        throw new RuntimeException("Failed to load macaw properties", e);
                }
        }
        return macawProperties;
    }
    
    
    public Properties loadMacawProperties() throws Exception {
        final String userHome = System.getProperty("user.home");
        final File macawConfFileDefault = new File(userHome, "macaw.conf");

        String preferredPath = System.getenv("MACAW_CONF_DIR");
        if (preferredPath == null || preferredPath.equals(""))
        {
                preferredPath = "/opt/cfx-config/common";
        }
        final File macawConfFilePreferred = new File(preferredPath, "macaw.conf");
        File macawConfFile = macawConfFilePreferred;

        if (!macawConfFile.exists() || !macawConfFile.isFile()) {
                macawConfFile = macawConfFileDefault;
        }

        if (macawConfFile.exists() && macawConfFile.isFile()) {
                logger.info("Loading macaw.conf from " + macawConfFile.getAbsolutePath());
                // load the properties
                try (final FileInputStream fileInputStream = new FileInputStream(
                                macawConfFile)) {
                        final Properties macawConfProperties = new Properties();
                        macawConfProperties.load(fileInputStream);
                        return macawConfProperties;
                }
        }
        return null;
    }
    
    public Properties loadMacawProperties(InputStream is) {
        
        macawProperties = new Properties();
        try {
        	macawProperties.load(is);
		} catch (IOException e) {
			logger.error("problem loading config file", e);
			return null;
		}
       
        return macawProperties;
    }

    public String getRequiredProperty(String propertyName, String errorMessage) {
        String value = macawProperties.getProperty(propertyName);
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException(errorMessage);
        }
        return value;
    }

    public String getProperty(String name, String defaultValue) {
    
            String value = macawProperties.getProperty(name, defaultValue);
            if (value == null) {
                    throw new IllegalArgumentException("Macaw property " + name + "does not exist");
            }
            return value;
    }
    
    public void initBucket(MinioClient client, String bucketName) {
        boolean isExist = false;
        try {
            isExist = client.bucketExists(bucketName);
            if( ! isExist ) {
                client.makeBucket(bucketName);
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getLocalizedMessage(), e);
        }
    }
    
    
}