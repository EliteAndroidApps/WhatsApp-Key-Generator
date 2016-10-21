# WhatsApp Key Generator
Demonstrates how WhatsApp generates and recovers cipher keys from encrypted backup files on Android devices.  


###### Usage:

              java -jar WhatsAppKeyGenerator.jar <Account Seed> <IMEI Number>  
              java -jar WhatsAppKeyGenerator.jar <Account Seed> <IMEI Number> <Encrypted Database Path>  
  
  Example #1 will generate a new key file and present it to you in visual form.  
  Example #2 will recover an existing key file from an encrypted database and present it to you in visual form.  
    
    
  
###### Notes:
  
  This tool is for demonstration purposes only and for helping the "curious" understand the relationship between the WhatsApp key file located at: "/data/data/com.whatsapp/files/key" and the encrypted WhatsApp database backup files.  
    
  Please do not raise any issues concerning what is used as the account seed. Figuratively speaking; you decide!
  
  

###### Credits:
 Author: TripCode
