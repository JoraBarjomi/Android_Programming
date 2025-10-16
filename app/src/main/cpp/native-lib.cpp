#include <iostream>
#include <string>
#include <stack>
#include <cmath>
#include <jni.h>

extern "C" JNIEXPORT jstring JNICALL
Java_com_example_mycalculator_MainActivity_calculate(
        JNIEnv* env,
        jobject,
        jstring input
) {
    const char* expr = env->GetStringUTFChars(input, 0);

    std::string line(expr);

    env->ReleaseStringUTFChars(input, expr);

    int len = line.length();
    std::string digit1;
    std::stack<double> digits;
    std::stack<char> operators;

    for(int i = 0; i < len; ++i){
        if(line[i] != '+' && line[i] != '-' && line[i] != '*' && line[i] != '/'){
            digit1 += line[i];
        } else{
            digits.push(stod(digit1));
            digit1 = "";

            int curr_priority = (line[i] == '*' || line[i] == '/') ? 2 : 1;

            while(!operators.empty()){
                int top_priority = (operators.top() == '*' || operators.top() == '/') ? 2 : 1;
                if(top_priority >= curr_priority){
                    double b = digits.top(); digits.pop();
                    double a = digits.top(); digits.pop();
                    char op = operators.top();
                    operators.pop();

                    switch (op) {
                        case '+':
                            digits.push(a + b);
                            break;
                        case '-':
                            digits.push(a - b);
                            break;
                        case '*':
                            digits.push(a * b);
                            break;
                        case '/':
                            if (b < 0) {
                                return env->NewStringUTF("Error!");
                            } else digits.push(a / b);
                            break;
                        }
                } else{
                    break;
                }
            }
            operators.push(line[i]);
        }
    }
    if(!digit1.empty()) digits.push(stod(digit1));

    while(!operators.empty()){
        double b = digits.top(); digits.pop();
        double a = digits.top(); digits.pop();

        char op = operators.top(); operators.pop();

        switch (op) {
            case '+': digits.push(a + b); break;
            case '-': digits.push(a - b); break;
            case '*': digits.push(a * b); break;
            case '/':
                if(b < 0){
                    return env->NewStringUTF("Error!");
                } else digits.push(a / b);
                break;
        }
    }

    double res = digits.top();

    char buffer[50];
    sprintf(buffer, "%.4f", res);
    return env->NewStringUTF(buffer);
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_example_mycalculator_MainActivity_procentage(
        JNIEnv* env,
        jobject,
        jstring input
) {
    const char *expr = env->GetStringUTFChars(input, 0);

    std::string line(expr);

    env->ReleaseStringUTFChars(input, expr);

    double res;

    if(line.find_first_of("+-*/") == std::string::npos){
         res = stod(line) / 100;
    } else {
        return env->NewStringUTF("Error! There are opreators!");
    }

    char buffer[50];
    sprintf(buffer, "%.4f", res);
    return env->NewStringUTF(buffer);
}

