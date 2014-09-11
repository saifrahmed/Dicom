Dicom
=====

Year 3 college project to display Dicom images using the PixelMed library

The purpose of this project is to display DICOM images using the PixelMed library.  

DICOM images are medical images that have been acquired through various modalities (CT Scan, X-Ray, etc)
Each DICOM file contains an image, or series of images, along with image and patient attributes.

To run this program, place the pixelmed.jar file in the %CLASSPATH% and compile/run

You will also need some DICOM images to test this; there are some in the /images folder and 
links for more are available on David Clunie's site http://www.dclunie.com/

Using Dicom Image viewer:

Click on "Source" the find the images you want to view
Once the tree has been populated you can double click on a file to view it's contents (image and attributes)

The "Destination" option is used when the image is to be anonymized.  A new file is created with certain
attributes anonymized so that a patient cannot be identified.

Another option is to just extract the image and save it as a jpeg, it will contain no image/patient attributes

