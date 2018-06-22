%pylab inline
import numpy as np
from sklearn import datasets, linear_model
from sklearn.metrics import mean_squared_error, r2_score
from sklearn.linear_model import perceptron
digits = np.loadtxt('digits_7_vs_9.csv', delimiter=' ')
# extract a stack of 28x28 bitmaps
X = digits[:, 0:784]

# extract labels for each bitmap
y = digits[:, 784:785]

# display a single bitmap and print its label
bitmap_index = 0
plt.imshow(X[bitmap_index,:].reshape(28, 28), interpolation=None)
print(y[bitmap_index])

def gallery(array, ncols):
    nindex, height, width = array.shape
    nrows = nindex//ncols
    result = (array.reshape((nrows, ncols, height, width))
              .swapaxes(1,2)
              .reshape((height*nrows, width*ncols)))
    return result

ncols = 10
result = gallery(X.reshape((300, 28, 28))[:ncols**2], ncols)
plt.figure(figsize=(10,10))
plt.imshow(result, interpolation=None)

#linear approach
net = perceptron.Perceptron(n_iter=100, verbose=0, random_state=None, fit_intercept=True, eta0=0.002)
net.fit(X,ravel(y))
w=net.coef_[0]
error=1-net.score(X,y)

print"error of the fit as the proportion of misclassified examples:" 
print error
plt.imshow(w.reshape(28,28), interpolation=None)

from sklearn.cross_validation import train_test_split
X_train, X_test,y_train,y_test=train_test_split(X,y,test_size=0.25,random_state=0)
trainerror=[]
testerror=[]
times=[]

def traindata(number):
    net = perceptron.Perceptron(n_iter=number, verbose=0, random_state=None, fit_intercept=True, eta0=0.002)
    net.fit(X_train,ravel(y_train))
    error_train=1-net.score(X_train,y_train)
    error_test=1-net.score(X_test,y_test)
    return error_train,error_test
for i in range(1,15):
    times.append(i+1)
    error_train,error_test=traindata(i)
    trainerror.append(error_train)
    testerror.append(error_test)
plot1=plt.plot(times, trainerror,label='Train error')
plot2=plt.plot(times, testerror,label='Test error' )
plt.xlabel('times')
plt.ylabel('error rates')
plt.legend()

#Basis Expansion
# Input:
# x - is a column vector of input values
# z - is a scalar that controls location
# s - is a scalar that controls spread
#
# Output:
# v - contains the values of RBF evaluated for each element x
#     v has the same dimensionality as x
def rbf(x, z, s):
    u1=np.array(x)
    v1=np.array(z)
    sub=u1-v1
    u_v=np.dot(sub,sub)
    uv_gamma=s*u_v
    result=np.exp(-uv_gamma)
    return result

def count_aline(x,Z,L,s):
    result = []
    for i in range(L):
        result.append(rbf(x,Z[i],s))
    return result

def RBF_1(X,Z,L,s):
    result = []
    num = int(X.shape[0])
    for i in range(num):
        result.append(count_aline(X[i],Z,L,s))
    return result

# s=5
X_train, X_test,y_train,y_test=cross_validation.train_test_split(X,y,test_size=0.25,random_state=0)
# locations and scale
s = 1 # same scale for each RBF
testerror=[]
minerror=1
times=[]
for L in range(1,200):
    Xtrain=RBF_1(X_train,X,L,s)
    Xtest=RBF_1(X_test,X,L,s)
    net.fit(Xtrain,ravel(y_train))
    error_test=1-net.score(Xtest,y_test)
    if (error_test<=minerror):minerror=error_test
    testerror.append(error_test)
for L1 in range(199):
    if (testerror[L1]==minerror): print ("When L is :" ,L1)
for i in range(1,200):
    times.append(i-1)
print ("The min error is :" ,minerror)
plot2=plt.plot(times, testerror,label='Test error' )
plt.xlabel('times')
plt.ylabel('error rates')
plt.legend()    

# locations and scale
L = 177 # L is 177
testerror1=[]
minerror1=1
times=[]
for s in range(1,100):
    Xtrain=RBF_1(X_train,X,L,s)
    Xtest=RBF_1(X_test,X,L,s)
    net.fit(Xtrain,ravel(y_train))
    error_test=1-net.score(Xtest,y_test)
    if (error_test<=minerror1):minerror1=error_test
    testerror1.append(error_test)
for L1 in range(99):
    if (testerror1[L1]==minerror1): print ("When s is :" ,L1)
for i in range(1,100):
    times.append(i-1)
print ("The min error is :" ,minerror1)
plot2=plt.plot(times, testerror1,label='Test error' )
plt.xlabel('times')
plt.ylabel('error rates')
plt.legend() 

#instead of directly computing a feature space transformation, we are going to use the kernel trick. 
#We are going to use the kernelised version of perceptron in combination with a few different kernels.
# Input:
# u,v - column vectors of the same dimensionality
#
# Output:
# v - a scalar
import numpy as np
def linear_kernel(u, v):
    result=np.dot(u,v)
    return result 
# Input:
# u,v - column vectors of the same dimensionality
# c,d - scalar parameters of the kernel as defined in lecture slides
#
# Output:
# v - a scalar
def polynomial_kernel(u, v, c=0, d=3):
    u_v=np.dot(u,v)
    u_v_add_c=u_v+c
    result=u_v_add_c**2
    return result
# Input:
# u,v - column vectors of the same dimensionality
# gamma - scalar parameter of the kernel as defined in lecture slides
#
# Output:
# v - a scalar
def rbf_kernel(u, v, gamma=1):
    ## your code here
    u1=np.array(u)
    v1=np.array(v)
    sub=u1-v1
    u_v=np.dot(sub,sub)
    uv_gamma=gamma*u_v
    result=np.exp(-uv_gamma)
    return result
# Input:
# x_test - (r x m) matrix with instances for which to predict labels
# X - (n x m) matrix with training instances in rows
# y - (n x 1) vector with labels
# alpha - (n x 1) vector with learned weigths
# bias - scalar bias term
# kernel - a kernel function that follows the same prototype as each of the three kernels defined above
#
# Output:
# y_pred - (r x 1) vector of predicted labels
def kernel_ptron_predict(x_test, X, y, alpha, bias, kernel):
    ## your code here
    sum_f=0.0
    for i in range(X.shape[0]):
            kernelaa=kernel(X[i],x_test)
            sum_f=sum_f+(alpha[i]*y[i]*kernelaa)
    result=sum_f+bias
    if result>=0: result=1
    else:result=-1
    return result
# Input:
# X - (n x m) matrix with training instances in rows
# y - (n x 1) vector with labels
# kernel - a kernel function that follows the same prototype as each of the three kernels defined above
# epochs - scalar, number of epochs
#
# Output:
# alpha - (n x 1) vector with learned weigths
# bias - scalar bias term
def kernel_ptron_train(X, y, kernel, epochs=100):
    n, m = X.shape
    alpha = np.zeros(n)
    bias = 0
    updates = None
    for epoch in range(epochs):
        print('epoch =', epoch, ', updates =', updates)
        updates = 0

        schedule = list(range(n))
        np.random.shuffle(schedule)
        for i in schedule:
            y_pred = kernel_ptron_predict(X[i], X, y, alpha, bias, kernel)
            
            if y_pred != y[i]:
                alpha[i] += 1
                bias += y[i]
                updates += 1

        if updates == 0:
            break
        
    return alpha, bias

# Now use the above functions to train the perceptron.
alpha_linear, bias_linear=kernel_ptron_train(X_train, y_train, linear_kernel, epochs=100)
correct=0
total=0
for i in range(X_test.shape[0]):
    if y_test[i]==kernel_ptron_predict(X_test[i], X_train, y_train, alpha, bias, linear_kernel):correct=correct+1
    total=total+1
print correct/float(total)
alpha, bias=kernel_ptron_train(X_train, y_train, polynomial_kernel, epochs=100)
correct_poly=0
total_poly=0
for i in range(X_test.shape[0]):
    if y_test[i]==kernel_ptron_predict(X_test[i], X_train, y_train, alpha, bias, polynomial_kernel):correct_poly=correct_poly+1
    total_poly=total_poly+1
print correct_poly/float(total_poly)
alpha, bias=kernel_ptron_train(X_train, y_train, rbf_kernel, epochs=100)
correct_rbf=0
total_rbf=0
for i in range(X_test.shape[0]):
    if y_test[i]==kernel_ptron_predict(X_test[i], X_train, y_train, alpha, bias, rbf_kernel):correct_rbf=correct_rbf+1
    total_rbf=total_rbf+1
print correct_rbf/float(total_rbf)
testpoly=[]
c_value=[]
for i in range(1,10):
    #def polynomial_kernel_test(u, v, c=4, d=6):
    def rbf_kernel_test(u, v, gamma=i):
    ## your code here
        u1=np.array(u)
        v1=np.array(v)
        sub=u1-v1
        u_v=np.dot(sub,sub)
        uv_gamma=gamma*u_v
        result=np.exp(-uv_gamma)
        return result
    alpha, bias=kernel_ptron_train(X_train, y_train, rbf_kernel_test, epochs=100)
    correct_poly=0
    total_poly=0
    for i in range(X_test.shape[0]):
        if y_test[i]==kernel_ptron_predict(X_test[i], X_train, y_train, alpha, bias, rbf_kernel_test):correct_poly=correct_poly+1
        total_poly=total_poly+1
    testpoly.append(correct_poly/float(total_poly))
    c_value.append(i)
times=[]
for i in range(1,10):
    times.append(i)
maxvalue=np.max(testpoly)
for j in range(9):
    if testpoly[j]==maxvalue:
        print ("When d is:",(j+1),",the Accuracy is highest")
        print ("The accuracy is",maxvalue)
plot1=plt.plot(times, testpoly,label='Accuracy')
plt.xlabel('value of d')
plt.ylabel('Accuracy of poly')
plt.legend()
times=[]
for i in range(1,10):
    times.append(i)
maxvalue=np.max(testpoly)
for j in range(9):
    if testpoly[j]==maxvalue:
        print ("When c is:",(j+1),",the Accuracy is highest")
        print ("The accuracy is",maxvalue)
plot1=plt.plot(times, testpoly,label='Accuracy')
plt.xlabel('value of c')
plt.ylabel('Accuracy of poly')
plt.legend()
times=[]
for i in range(1,10):
    times.append(i)
maxvalue=np.max(testpoly)
for j in range(9):
    if testpoly[j]==maxvalue:
        print ("When gamma is:",(j+1),",the Accuracy is highest")
        print ("The accuracy is",maxvalue)
plot1=plt.plot(times, testpoly,label='Accuracy')
plt.xlabel('value of gamma')
plt.ylabel('Accuracy of rbf')
plt.legend()

# Dimensionality Reduction
from sklearn import manifold

X = digits[:, 0:784]
y = np.squeeze(digits[:, 784:785])

# n_components refers to the number of dimensions after mapping
# n_neighbors is used for graph construction
X_iso = manifold.Isomap(n_neighbors=30, n_components=2).fit_transform(X)

# n_components refers to the number of dimensions after mapping
embedder = manifold.SpectralEmbedding(n_components=2, random_state=0)
X_se = embedder.fit_transform(X)

f, (ax1, ax2) = plt.subplots(1, 2)
ax1.plot(X_iso[y==-1,0], X_iso[y==-1,1], "bo")
ax1.plot(X_iso[y==1,0], X_iso[y==1,1], "ro")
ax1.set_title('Isomap')
ax2.plot(X_se[y==-1,0], X_se[y==-1,1], "bo")
ax2.plot(X_se[y==1,0], X_se[y==1,1], "ro")
ax2.set_title('spectral')

from sklearn import cross_validation
import numpy as np
from sklearn import datasets, linear_model
from sklearn.metrics import mean_squared_error, r2_score
from sklearn.linear_model import perceptron
X = digits[:, 0:784]
y = np.squeeze(digits[:, 784:785])
X_iso = manifold.Isomap(n_neighbors=30, n_components=2).fit_transform(X)
X_train, X_test,y_train,y_test=train_test_split(X_iso,y,test_size=0.25,random_state=0)
trainerror=[]
testerror=[]
times=[]
def traindata(number):
    net = perceptron.Perceptron(n_iter=number, verbose=0, random_state=None, fit_intercept=True, eta0=0.002)
    net.fit(X_train,ravel(y_train))
    error_train=1-net.score(X_train,y_train)
    error_test=1-net.score(X_test,y_test)
    return error_train,error_test

for i in range(1,10):
    times.append(i+1)
    error_train,error_test=traindata(i)
    trainerror.append(error_train)
    testerror.append(error_test)
plot1=plt.plot(times, trainerror,label='Train error')
plot2=plt.plot(times, testerror,label='Test error' )
plt.xlabel('times')
plt.ylabel('error rates')
plt.legend()


# Handwritten Digit Recognition
# This is my code for this part.
# It can generate a file:1 time.csv
# The result is my best score on Kaggle
import csv
from keras.models import Sequential
from keras.layers import Conv2D, MaxPooling2D, Flatten  
from keras.optimizers import SGD    
from keras.layers import Dense
from keras.layers import Dropout
from keras.layers import Flatten
from keras.utils import np_utils 


X_train = np.load('train_X.npy')
X_label = np.load('train_y.npy')
X_test = np.load('test_X.npy')

seed = 7
np.random.seed(seed)

X_train = X_train.reshape(X_train.shape[0], 64, 64, 1).astype('float32')
X_test = X_test.reshape(X_test.shape[0], 64, 64, 1).astype('float32')

X_train = X_train / 255
X_test = X_test / 255
# one hot encode outputs

X_label = np_utils.to_categorical(X_label)


num_classes = X_label.shape[1]


def createmodel():
    model = Sequential()
    model.add(Conv2D(filters=6, kernel_size=(5,5),padding='valid',input_shape=(64, 64, 1), activation='relu'))
    model.add(MaxPooling2D(pool_size=(2, 2)))
    model.add(Conv2D(filters=16, kernel_size=(5,5), padding='valid', activation='relu'))
    model.add(MaxPooling2D(pool_size=(2, 2)))
    model.add(Dropout(0.2))
    model.add(Flatten())
    model.add(Dense(120, activation='relu'))
    model.add(Dense(84, activation='relu'))
    model.add(Dense(num_classes, activation='softmax'))
    # Compile model
    # model.summary()
    sgd = SGD(lr=0.05, momentum=0.9, decay=1e-6, nesterov=True)  
    model.compile(loss='categorical_crossentropy', optimizer=sgd, metrics=['accuracy'])
    return model
result = createmodel()
result.fit(X_train, X_label, epochs=50, batch_size=200, verbose=1)
final = result.predict(X_test)
final = final.argmax(1)
with open('1 time.csv','w')as f:
    myWriter = csv.writer(f)
    for i in final:
        a=[]
        a.append(i)
        myWriter.writerow(a)
