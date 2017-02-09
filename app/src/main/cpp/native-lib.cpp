#include <jni.h>
#include <opencv2/opencv.hpp>

using namespace cv;
using namespace std;

extern "C" {

JNIEXPORT void JNICALL
Java_kr_co_ipdisk_home35_PRIMA_MainActivity_loadImage(
        JNIEnv *env,
        jobject,
        jstring imageFileName,
        jlong addrImage) {

    Mat &img_input = *(Mat *) addrImage;

    const char *nativeFileNameString = env->GetStringUTFChars(imageFileName, JNI_FALSE);

    string baseDir("/storage/emulated/0/");
    baseDir.append(nativeFileNameString);
    const char *pathDir = baseDir.c_str();

    img_input = imread(pathDir, IMREAD_COLOR);

}

JNIEXPORT void JNICALL
Java_kr_co_ipdisk_home35_PRIMA_MainActivity_imageprocessing(
        JNIEnv *env,
        jobject,
        jlong addrInputImage,
        jlong addrOutputImage,
        jlong addrInterest) {
    Rect rect,temp_rect;
    vector<vector<Point> > contours;
    vector<Vec4i> hierarchy;
    double ratio,delta_x,delta_y,gradient;
    int select,plate_width,count,friend_count = 0, refinery_count=0;
    Mat image2,image3;
    Mat &img_input = *(Mat *) addrInputImage;
    Mat &img_output = *(Mat *) addrOutputImage;
    Mat drawing;

    cvtColor( img_input, img_input, CV_BGR2RGB);
    img_input.copyTo(image2);
    img_input.copyTo(image3);
    cvtColor( image2, image2, CV_RGB2GRAY);
    Canny(image2,image2,100,100,3);
    //adaptiveThreshold(img_output, img_output,
    //                 255, ADAPTIVE_THRESH_MEAN_C, THRESH_BINARY, 5, 10);
    findContours(image2,contours,hierarchy,CV_RETR_TREE,CV_CHAIN_APPROX_SIMPLE,Point());

    vector<vector<Point> > contours_Poly(contours.size());
    vector<Rect>boundRect(contours.size());
    vector<Rect>boundRect2(contours.size());

    for(  int i=0;i<contours.size();i++) {
        approxPolyDP(Mat(contours[i]), contours_Poly[i], 1, true);
        boundRect[i] = boundingRect(Mat(contours_Poly[i]));
    }
    drawing = Mat::zeros(image2.size(), CV_8UC3);
    for(int i=0; i<contours.size();i++){
        ratio = (double)boundRect[i].height / boundRect[i].width;

        if((ratio<=2.5) && (ratio >=0.5) && (boundRect[i].area()<=700)&&(boundRect[i].area()>=100)){
            drawContours(drawing,contours,i,Scalar(0,255,255),1,8,hierarchy,0,Point());
            rectangle(drawing,boundRect[i].tl(),boundRect[i].br(),Scalar(255,0,0),1,8,0);

            boundRect2[refinery_count] = boundRect[i];
            refinery_count+=1;

        }
    }
    boundRect2.resize(refinery_count);  //  Resize refinery rectangle array.

    //  Bubble Sort accordance with X-coordinate.
    for(int i=0; i<boundRect2.size(); i++){
        for(int j=0; j<(boundRect2.size()-i); j++){
            if(boundRect2[j].tl().x > boundRect2[j+1].tl().x){
                temp_rect = boundRect2[j];
                boundRect2[j] = boundRect2[j+1];
                boundRect2[j+1] = temp_rect;
            }
        }
    }


    for(int i = 0; i< boundRect2.size(); i++){

        rectangle(image3, boundRect2[i].tl(), boundRect2[i].br(), Scalar(0,255,0), 1, 8, 0);

        count = 0;

        //  Snake moves to right, for eating his freind.
        for(int j=i+1; j<boundRect2.size(); j++){

            delta_x = abs(boundRect2[j].tl().x - boundRect2[i].tl().x);

            if(delta_x > 150)  //  Can't eat snake friend too far ^-^.
                break;

            delta_y = abs(boundRect2[j].tl().y - boundRect2[i].tl().y);


            //  If delta length is 0, it causes a divide-by-zero error.
            if(delta_x == 0){
                delta_x = 1;
            }

            if(delta_y == 0){
                delta_y = 1;
            }


            gradient = delta_y / delta_x;  //  Get gradient.
            cout << gradient << endl;

            if(gradient < 0.25){  //  Can eat friends only on straight line.
                count += 1;
            }
        }

        //  Find the most full snake.
        if(count > friend_count){
            select = i;  //  Save most full snake number.
            friend_count = count;  //  Renewal number of friends hunting.
            rectangle(image3, boundRect2[select].tl(), boundRect2[select].br(), Scalar(255,0,0), 1, 8, 0);
            plate_width = delta_x;  //  Save the last friend ate position.
        }                           //  It's similar to license plate width, Right?
    }

    //  Drawing most full snake friend on the image.
<<<<<<< HEAD
    rectangle(image3, boundRect2[select].tl(), boundRect2[select].br(), Scalar(0, 0, 255), 2, 8, 0);
    line(image3, boundRect2[select].tl(),
         Point(boundRect2[select].tl().x + plate_width, boundRect2[select].tl().y),
         Scalar(0, 0, 255), 1, 8, 0);
    img_output = image3;


    interest = img_input(Rect(boundRect2[select].tl().x - 20, boundRect2[select].tl().y - 20,
                           plate_width + 40, plate_width * 0.3));


    vector<int> compression_params;
    compression_params.push_back(CV_IMWRITE_PNG_COMPRESSION);
    compression_params.push_back(9);
    imwrite("interest.jpg", interest, compression_params);
=======
    rectangle(image3, boundRect2[select].tl(), boundRect2[select].br(), Scalar(0,0,255), 2, 8, 0);
    line(image3, boundRect2[select].tl(), Point(boundRect2[select].tl().x+plate_width, boundRect2[select].tl().y), Scalar(0,0,255), 1, 8, 0);
    img_output=image3;
    imwrite("/storage/emulated/0/test.jpg",
            img_input(Rect(boundRect2[select].tl().x-20, boundRect2[select].tl().y-20, plate_width+50, plate_width*0.4)));
>>>>>>> origin/master

}
}

