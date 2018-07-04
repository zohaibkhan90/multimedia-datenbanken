# README #



### What is this repository for? ###

* Multimedia Databases Project - SS18
* 1.0

### How do I get set up? ###

* Import the project in eclipse using "import existing maven project" option
* Open src/org/lira/IndexService.java file, right click and run as Java Application
* All the required resources are with in the project
* Images dataset can be found under image.vary.jpg folder
* Some test images can be found in resources folder, along with a properties file: resources/config.properties
* config.properties file can be used to reconfigure the project. Below is the details for available properties in this file

| Property name | Description | Default Value |
|------------------ | ---------------------------------------------------- |-----------------------|
| base_path| Set it's value to the directory where you have | image.vary.jpg/ |
| | placed images data set for adding in index | |
|------------------ | ---------------------------------------- | -----------------------|
| reindex | Set it's value to true if you want the reindex | true |
| | images, false otherwise. If you want to avoid | |
| | indexing to run, set it to false | |
|------------------ | ---------------------------------------- | ----------------------|
| index_name | You can set it's value to name that you want to | images_index |
| | be used as your index, run the IndexService.java | |
| | again to create this index | |
|------------------ | ----------------------------------------- | ----------------------|
| num_of_results | Set it's value to an integer, to choose how many | 12 |
| | matching images you want in result from the | |
| | image search | |
|------------------ | ----------------------------------------- | ----------------------|
| index_dir | All your indexes will be created in this | indexes/ |
| | directory, you can select any name to keep you | |
| | indexes in that directory | |
|------------------ | ----------------------------------------- | ----------------------|
| test_image | Set it's value to a path of an image which will | resources/imageA.jpg |
| | be used to test insert function, it's default | |
| | image is included in resources | |
