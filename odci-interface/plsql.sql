--Drop table "IMG_TABLE";
--Drop OPERATOR is_lucene_similar ;
--Drop FUNCTION bt_is_lucene_similar;
--Drop TYPE BODY ODCIIndex;
--Drop TYPE ODCIIndex force;
--Drop INDEXTYPE LIREINDEXTYPE;



CREATE OR REPLACE TYPE ODCIIndex AS OBJECT
(
  key INTEGER,

  STATIC FUNCTION ODCIGetInterfaces(
    ifclist OUT sys.ODCIObjectList)
  RETURN NUMBER,

  STATIC FUNCTION ODCIIndexCreate (ia SYS.ODCIIndexInfo, parms VARCHAR2,
    env SYS.ODCIEnv) RETURN NUMBER
    AS LANGUAGE JAVA
    NAME 'org.odci.oracle.impl.ODCIIndex.ODCIIndexCreate(oracle.ODCI.ODCIIndexInfo, java.lang.String,
    oracle.ODCI.ODCIEnv) return java.math.BigDecimal',

  -- STATIC FUNCTION ODCIIndexAlter (ia sys.ODCIIndexInfo, 
  --   parms IN OUT VARCHAR2, altopt number, env sys.ODCIEnv) RETURN NUMBER, 

  STATIC FUNCTION ODCIIndexDrop(ia SYS.ODCIIndexInfo, env SYS.ODCIEnv) 
    RETURN NUMBER
    AS LANGUAGE JAVA
    NAME 'org.odci.oracle.impl.ODCIIndex.ODCIIndexDrop(oracle.ODCI.ODCIIndexInfo, oracle.ODCI.ODCIEnv)
    return java.math.BigDecimal',

  -- STATIC FUNCTION ODCIIndexExchangePartition(ia SYS.ODCIIndexInfo,
  --   ia1 SYS.ODCIIndexInfo, env SYS.ODCIEnv) RETURN NUMBER,

  -- STATIC FUNCTION ODCIIndexUpdPartMetadata(ia sys.ODCIIndexInfo, 
  --   palist sys.ODCIPartInfoList, env sys.ODCIEnv) RETURN NUMBER,

  STATIC FUNCTION ODCIIndexInsert(ia SYS.ODCIIndexInfo, rid VARCHAR2,
    newval BFILE, env SYS.ODCIEnv) RETURN NUMBER
    AS LANGUAGE JAVA
    NAME 'org.odci.oracle.impl.ODCIIndex.ODCIIndexInsert(oracle.ODCI.ODCIIndexInfo, java.lang.String, 
    oracle.jdbc.OracleBfile, oracle.ODCI.ODCIEnv) return java.math.BigDecimal',

  STATIC FUNCTION ODCIIndexDelete(ia SYS.ODCIIndexInfo, rid VARCHAR2,
    oldval BFILE, env SYS.ODCIEnv) RETURN NUMBER
    AS LANGUAGE JAVA
    NAME 'org.odci.oracle.impl.ODCIIndex.ODCIIndexDelete(oracle.ODCI.ODCIIndexInfo, java.lang.String, 
    oracle.jdbc.OracleBfile, oracle.ODCI.ODCIEnv) return java.math.BigDecimal',

  STATIC FUNCTION ODCIIndexUpdate(ia SYS.ODCIIndexInfo, rid VARCHAR2,
    oldval BFILE, newval BFILE, env SYS.ODCIEnv) RETURN NUMBER
    AS LANGUAGE JAVA
    NAME 'org.odci.oracle.impl.ODCIIndex.ODCIIndexUpdate(oracle.ODCI.ODCIIndexInfo, java.lang.String, 
    oracle.jdbc.OracleBfile, oracle.jdbc.OracleBfile, oracle.ODCI.ODCIEnv) return 
    java.math.BigDecimal',

  STATIC FUNCTION ODCIIndexStart(sctx IN OUT ODCIIndex, ia SYS.ODCIIndexInfo,
    op SYS.ODCIPredInfo, qi sys.ODCIQueryInfo, strt number, stop number,
    cmpval VARCHAR2, env SYS.ODCIEnv) RETURN NUMBER
    AS LANGUAGE JAVA
    NAME 'org.odci.oracle.impl.ODCIIndex.ODCIIndexStart(org.odci.oracle.impl.ODCIIndex[], oracle.ODCI.ODCIIndexInfo, 
    oracle.ODCI.ODCIPredInfo, 
    oracle.ODCI.ODCIQueryInfo, java.math.BigDecimal, 
    java.math.BigDecimal, 
                java.lang.String, oracle.ODCI.ODCIEnv) return java.math.BigDecimal',

  MEMBER FUNCTION ODCIIndexFetch(nrows NUMBER, rids OUT SYS.ODCIridlist,
    env SYS.ODCIEnv) RETURN NUMBER
    AS LANGUAGE JAVA
    NAME 'org.odci.oracle.impl.ODCIIndex.ODCIIndexFetch(java.math.BigDecimal, 
    oracle.ODCI.ODCIRidList[], oracle.ODCI.ODCIEnv) return java.math.BigDecimal',

  MEMBER FUNCTION ODCIIndexClose(env SYS.ODCIEnv) RETURN NUMBER
    AS LANGUAGE JAVA
    NAME 'org.odci.oracle.impl.ODCIIndex.ODCIIndexClose(oracle.ODCI.ODCIEnv) return java.math.BigDecimal'
);
/





CREATE OR REPLACE TYPE BODY ODCIIndex AS 
  STATIC FUNCTION ODCIGetInterfaces(ifclist OUT sys.ODCIObjectList) 
  RETURN NUMBER IS
  BEGIN
    ifclist := sys.ODCIObjectList(sys.ODCIObject('SYS','ODCIINDEX2'));
    return ODCIConst.Success;
  END ODCIGetInterfaces;
END;
/

CREATE OR REPLACE FUNCTION f_similarity(a BFILE, b VARCHAR2) RETURN NUMBER AS
BEGIN 
  RETURN 1;
END;
/

--drop function f_similarity;


CREATE OR REPLACE OPERATOR similarity 
BINDING (BFILE, VARCHAR2) RETURN NUMBER 
USING f_similarity;

--drop operator similarity;





CREATE OR REPLACE INDEXTYPE LIREINDEXTYPE
FOR
similarity (BFILE, VARCHAR2)
USING ODCIIndex;


CREATE TABLE "IMG_TABLE" ( "COLUMN1" BFILE );

CREATE INDEX "LIRE" ON "IMG_TABLE" ("COLUMN1")
INDEXTYPE IS "LIREINDEXTYPE" ;



--DROP INDEX "LIRE";


CREATE OR REPLACE DIRECTORY IMG_DIR AS '/home/oracle/Desktop/image_files';

INSERT INTO "IMG_TABLE" ("COLUMN1") VALUES (BFILENAME('IMG_DIR', '1.jpg'));


--select * from IMG_TABLE;

--DELETE FROM IMG_TABLE;








DECLARE 
   a number(6) := 1; 
BEGIN 
   WHILE a < 9908 LOOP 
      INSERT INTO "IMG_TABLE" ("COLUMN1") VALUES (BFILENAME('IMG_DIR', CONCAT(a,'.jpg')));
      a := a + 1; 
   END LOOP; 
END; 
/


--Once all images inserted in database, uncomment and run below query to get similarity results
SELECT similarity(COLUMN1, '/home/oracle/Desktop/image_files/0.jpg') as sim, COLUMN1
FROM IMG_TABLE WHERE similarity(COLUMN1, '/home/oracle/Desktop/image_files/0.jpg') > 0;
