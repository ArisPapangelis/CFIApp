# -*- coding: utf-8 -*-
"""
Created on Sun Dec 27 04:03:56 2020

@author: Aris Papangelis

Function that extracts the CFI curve from each meal, as well as the in-meal indicators

"""

import numpy as np
import matplotlib.pyplot as plt
from scipy.optimize import curve_fit
import io
#from os.path import dirname, join


def fit_func(x, a, b):
  return a * x ** 2 +  b * x 


#https://stackoverflow.com/questions/24885092/finding-the-consecutive-zeros-in-a-numpy-array
def zero_runs(a):
    # Create an array that is 1 where a is 0, and pad each end with an extra 0.
    iszero = np.concatenate(([0], np.equal(a, 0).view(np.int8), [0]))
    absdiff = np.abs(np.diff(iszero))
    # Runs start and end where absdiff is 1.
    ranges = np.where(absdiff == 1)[0].reshape(-1, 2)
    return ranges





def extract_cfi(t, w, end_of_meal, stable_secs, meal_ID, plate_weight, portrait_mode):
    #def extract_cfi(file, initial_sampling_rate, end_of_meal, stable_secs, to, meal_ID):


    #filepath = join(dirname(__file__), file + ".txt")
    #time, cfi = np.loadtxt(filepath, delimiter = ':', skiprows=1, unpack = True)
    time = np.array(t)
    #time = time - time[0]
    cfi = np.array(w)
    
    
    #Downsampling 10Hz to 5Hz
    downsampled_rate = 5
    #downsampling_factor = int(initial_sampling_rate / downsampled_rate)
    #time = time[::downsampling_factor]
    #cfi = cfi[::downsampling_factor]
    #time = time[:to]
    #cfi = cfi[:to]
    
    #cfi_raw = cfi.copy()
    
    plt.ioff()
    if portrait_mode==False:
        plt.figure(meal_ID, figsize=(26, 10.8))
    else:
        plt.figure(meal_ID, figsize=(10.8, 9))

    plt.xlabel('Time (seconds)', fontsize = 25)
    plt.ylabel('Weight (grams)', fontsize = 25)
    plt.xlim(0,1000)
    plt.ylim(0,700)
    plt.title(meal_ID, fontsize = 30)
    plt.xticks(fontsize = 22)
    plt.yticks(fontsize = 22)
    plt.plot(time,cfi, label = "Initial data")
    
    
    
    #Find stable periods
    stability_threshold = 1
    diff = abs(np.diff(cfi))
    diff = np.where(diff <= stability_threshold, 0, diff)
    ranges = zero_runs(diff)
    
    #Keep only the stable periods that last more than stable_secs seconds
    stable_period_length = stable_secs * downsampled_rate
    diffranges = np.diff(ranges,axis=1)
    ranges = ranges[np.where(diffranges>=stable_period_length)[0]]
    
    
    #Set the same sample value for each stable period, to eliminate jitter
    cfi[ranges[0,0]:ranges[0,1]+1] = np.median(cfi[ranges[0,0]:ranges[0,1]+1]) 
    for i in range(1,len(ranges)):
            cfi[ranges[i,0]+1:ranges[i,1]+1] = np.median(cfi[ranges[i,0]+1:ranges[i,1]+1]) 
    
            
    #Delta coefficients
    if time[-1] < 60:
        secs = int(time[-1])
    else:
        secs = 60
        
    D = int(secs / 2 * downsampled_rate)
    taps = np.arange(-D, D + 1)
    denominator = np.sum(np.square(taps))
    fir = taps / denominator
    
    delta = -100 * np.convolve(fir,cfi, mode = 'valid')
    
    padding = int((len(cfi)-len(delta)) / 2)
    delta = np.pad(delta, (padding,padding), mode = 'edge')
    
    #plt.plot(time,delta, label = "Delta")
    

    #Compare stable periods to eliminate artifacts or identify food additions
    food_mass_bite_threshold = 75
    food_addition_threshold = 60
    food_mass_bite_count = 0
    for i in range(1,len(ranges)):
                
        #Food addition
        second_to_first_difference = cfi[ranges[i,0]+1]-cfi[ranges[i-1,0]+1]
        if second_to_first_difference > food_addition_threshold:
            deltaDiff = delta[ranges[i,0]+1] - delta[ranges[i-1,0]+1]
            #deltaDiff>=0 and 
            if deltaDiff>=0 and delta[ranges[i,0]+1] > 0 :
                for j in range(ranges[i-1,1]+1):
                    cfi[j] = cfi[j] + second_to_first_difference
            else:
                cfi[ranges[i,0]+1:ranges[i,1]+1] = cfi[ranges[i-1,0]+1]
        
        
        #Large food mass bite
        elif second_to_first_difference < -food_mass_bite_threshold:
            #if i<len(ranges)-1 and cfi[ranges[i+1,0]+1]-cfi[ranges[i,0]+1]>10:
            cfi[ranges[i,0]+1:ranges[i,1]+1] = cfi[ranges[i-1,0]+1]
            food_mass_bite_count += 1
        
    
        #Artifacts
        elif second_to_first_difference>0:
            
            #Final food mass bites of the meal
            if i>1 and cfi[ranges[i,0]+1] - cfi[ranges[i-2,0]+1] < 0 and food_mass_bite_count > 5:
                 cfi[ranges[i-1,0]+1:ranges[i-1,1]+1] = cfi[ranges[i-2,0]+1]
            
            #Utensilising artifact     
            else:     
                cfi[ranges[i,0]+1:ranges[i,1]+1] = cfi[ranges[i-1,0]+1]
        


    #Find stable samples
    #https://stackoverflow.com/questions/6036837/a-numpy-arange-style-function-with-array-inputs
    stableSamples = np.concatenate([np.arange(x, y) for x, y in zip(ranges[:,0]+1, ranges[:,1]+1)])
    stableSamples = np.concatenate((np.array([stableSamples[0]-1]), stableSamples))
    
    
    #Set unstable samples equal to previous stable
    for i in range(1,len(cfi)):
        if i not in stableSamples:
            cfi[i]=cfi[i-1]
            
    
    
    #"""
    #Start and end of meal
    cfi = cfi[ranges[0,0]:ranges[len(ranges)-1,1]]
    time = time[ranges[0,0]:ranges[len(ranges)-1,1]]
    if end_of_meal == True:
        #print(meal_ID)
        indices = np.where(np.diff(cfi)!=0)[0]
        if len(indices) != 0:
            startIndex = indices[0]
            endIndex = indices[-1]
            start = startIndex - 20 if startIndex - 20 >= 0 else startIndex - 10
            end = endIndex + 20 if endIndex + 20 < len(cfi) else endIndex + 10
            cfi = cfi[start:end]
            time = time[start:end]
    
    #"""
    index_offset = time[0] * downsampled_rate
    time = time - time[0]

    #Plot reference curve if training mode was selected
    if plate_weight > 5:
        end_weight = cfi[0] - plate_weight
        reference_coeff = [-0.001, 1, -end_weight]
        candidate_times = np.roots(reference_coeff)
        actual_root = np.real(np.min(candidate_times))
        reference_time = np.arange(0, actual_root, 1/downsampled_rate)
        reference_curve = reference_coeff[0] * reference_time ** 2 + reference_coeff[1] * reference_time
        plt.plot(reference_time, reference_curve, label= "Reference curve")


    #Remove plate weight and invert CFI curve
    cfi = cfi-cfi[-1]
    cfi = abs(cfi-cfi[0])
    
    #Join consecutive stable periods of the same weight (eliminate bites below stability threshold)
    bites = np.diff(cfi)
    for i in np.where((bites!=0) & (bites <= stability_threshold))[0]:
        j = i+1
        weight = cfi[j]
        while (j<len(cfi) and cfi[j] == weight):
            cfi[j] = cfi[i]
            j+=1 
       
    
    #Fit the extracted CFI curve to a second degree polynomial
    coefficients = curve_fit(fit_func,time,cfi)[0]
    #coefficients[-1] = 0
    curve = coefficients[0] * time ** 2 + coefficients[1] * time

   
    
    #a, b, total food intake, average food intake rate, average bite size and standard deviation, bites per minute
    a = coefficients[0]
    b = coefficients[1]
    meal_duration = time[-1]
    total_food_intake = cfi[-1]
    average_food_intake_rate = total_food_intake / meal_duration
    
    bites = np.diff(cfi)
    bite_indices = np.where(bites!=0)[0]
    bites = bites[bite_indices]
    average_bite_size = np.mean(bites)
    bite_size_STD = np.std(bites)
    bite_frequency = 60 * len(bites) / meal_duration

    
    #Plot extracted cfi curve
    plt.plot(time,curve, label = "Quadratic curve", linewidth = 5, linestyle = '-')
    plt.plot(time,cfi, label = "Extracted CFI", linewidth = 4, alpha=0.6)
    #plt.scatter((bite_indices + int(index_offset)) / downsampled_rate, cfi_raw[bite_indices + int(index_offset)], label = 'Detected bites', c = 'tab:orange')
    plt.legend(loc=2, fontsize=20)

    f = io.BytesIO()
    plt.savefig(f, format="png")
    #plt.show()
    plt.close(meal_ID)
    if end_of_meal==True and portrait_mode==False:
        results = np.array([a, b, total_food_intake, average_food_intake_rate, average_bite_size, bite_size_STD, bite_frequency])
        return results
    else:
        return f.getvalue()
