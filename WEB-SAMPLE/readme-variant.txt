Copyright 2015 Variant Inc.

Variant Related Changes:

1. Maven dependencies: groupId: com.variant; artifactIds: variant-core, variant-web; version: 0.5.
2. Add variant-schema.json to src/resources. Can go anywhere, really - just for the sake of demo.
3. Crate new experiences: 
  3.1. Add 2 mappings to OwnerController.java
  3.2. Add 2 corresponding JSP files in src/main/webapp/WEB-INF/jsp/owners

4. Create a new Serlet Filter VariantFilter
5. Add <logger name="com.variant" level="debug"/> to logback.xml. (Optional)

6. Created test com.variant.web.sample.smvc4.EventDataPopulator to simulate thousands of sessions'
   outoput to the database.
7. Created script exportToCsv.sh to dump the output of prev. step to a csv file. (Tableau does not
   support DB sources.)