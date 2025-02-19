import re
import numpy as np
import matplotlib.pyplot as plt
import os
import cv2
import statistics
import pandas as pd
debug = False
file1 = "../results/1QTable/profile/actions.txt"
file2 = "../results/2QTables/profile/actions.txt"
output_folder = "../results/"

## Remove strings 

def remove_strings_from_file(file_name, strings_to_remove):
    try:
        with open(file_name, 'r') as file:
            lines = file.readlines()

        # Open a new file for writing
        with open(file_name, 'w') as f:
            # Iterate through each line in the input file
            for line in lines:
                # Split the line by whitespace and keep only the elements from index 1 onwards
                elements = line.split(' ')[1:]
                # Join the elements back into a string and write to the output file
                f.write(' '.join(elements) + '\n')
                
        with open(file_name, 'w') as file:
            for line in lines:
                for string_to_remove in strings_to_remove:
                    line = line.replace(string_to_remove, '')
                    line = line.replace(',', '\n')
                file.write(line)
        
        print("Strings removed successfully!")
    except FileNotFoundError:
        print(f"File '{file_name}' not found.")

## Clean Data

# List of strings to remove
strings_to_remove = [
    "[","]","Exp number:", "Action num: ", "Battery: ", "reward: ",
    "Curiosity_lv: ", "Red: ", "Green: ", "Blue: ","action:","mot_value: ",
    "r_imp: ","g_imp: ","b_imp: ", "hug_drive: ", "cur_drive: ", "QTables:", 
    "Exp:", "Nact:", "Type:"
]

def replace(results):
    aux = []
    for element in results:
        for i in range(len(element)):
            if(type(element[i])== str): element[i] = element[i].replace('\n', '')
        aux.append(element)
    return aux

m_i = False

def get_data_drives(file,exp):
    n_action = []
    exps = []
    actions = []
    mot = []
    am0 =  []
    am1 =  []
    am2 =  []
    am3 =  [] 
    am4 =  []
    am5 =  []
    am6 =  []
    am7 =  []
    am8 =  []
    am9 =  []
    am10 =  []
    am11 =  []
    am12 =  []
    am13 =  []
    aa0 =  []
    aa1 =  []
    aa2 =  []
    am14 =  []
    am15 =  []
    am16 =  []
    # Dicionário para mapear os valores únicos do array aos seus índices
   

    with open(file,"r") as f: # Open file
        #name = file.split('.')[0] 
        #last_name = name.split('/')[2] 
        data = f.readlines()
        aux = 0
        i = 0
        for line in data:
            if(aux == 0):
                aux+=1
            else:     
                col = line.split(' ') 
                if(len(col)>7): col = col[1:]
                #print(col[1])
                if(int(col[2])==exp):
                    if m_i:
                        n_action.append(col[1])
                        actions.append(int(col[3])+250)
                        mot.append(col[4])
                       
                    else: 

                        n_action.append(col[1])
                        actions.append(int(col[3]))
                        if debug: print(col)
                        mot.append(col[4])

                        
                    if col[1] == "am0":
                        am0.append(1)
                    else: am0.append(0)  

                    if col[1] == "am1":
                        am1.append(1)
                    else: am1.append(0)  

                    if col[1] == "am2":
                        am2.append(1)
                    else: am2.append(0)  

                    if col[1] == "am3":
                        am3.append(1)
                    else: am3.append(0)  

                    if col[1] == "am4":
                        am4.append(1)
                    else: am4.append(0)  

                    if col[1] == "am5":
                        am5.append(1)
                    else: am5.append(0)  

                    if col[1] == "am6":
                        am6.append(1)
                    else: am6.append(0)  

                    if col[1] == "am7":
                        am7.append(1)
                    else: am7.append(0)  

                    if col[1] == "am8":
                        am8.append(1)
                    else: am8.append(0)  

                    if col[1] == "am9":
                        am9.append(1)
                    else: am9.append(0)  

                    if col[1] == "am10":
                        am10.append(1)
                    else: am10.append(0)  

                    if col[1] == "am11":
                        am11.append(1)
                    else: am11.append(0)  

                    if col[1] == "am12":
                        am12.append(1)
                    else: am12.append(0)  

                    if col[1] == "am13":
                        am13.append(1)
                    else: am13.append(0)  

                    if col[1] == "am14":
                        am14.append(1)
                    else: am14.append(0)  

                    if col[1] == "am15":
                        am15.append(1)
                    else: am15.append(0)  

                    if col[1] == "am16":
                        am16.append(1)
                    else: am16.append(0)    

                    if col[1] == "aa0":
                        aa0.append(1)
                    else: aa0.append(0)    

                    if col[1] == "aa1":
                        aa1.append(1)
                    else: aa1.append(0)    

                    if col[1] == "aa2":
                        aa2.append(1)
                    else: aa2.append(0) 
                    exps.append(i)

                    i+=1
    actions = [am0, am1, am2, am3, am4, am5, am6, am7, am8, am9, am10, am11, am12, am13, am14, am15, am16, aa0, aa1, aa2]                   
    return [n_action, exps, actions, mot, actions] # len 12



def get_data_impulses(file, exp):
    am0 =  []
    am1 =  []
    am2 =  []
    am3 =  [] 
    am4 =  []
    am5 =  []
    am6 =  []
    am7 =  []
    am8 =  []
    am9 =  []
    am10 =  []
    am11 =  []
    am12 =  []
    am13 =  []
    aa0 =  []
    aa1 =  []
    aa2 =  []
    am14 =  []
    am15 =  []
    am16 =  []
    n_action = []
    exps = []
    actions = []
    mot = []
    with open(file,"r") as f: # Open file
        #name = file.split('.')[0] 
        #last_name = name.split('/')[2] 
        data = f.readlines()
        aux = 0
        i = 0
        for line in data:
            if(aux == 0):
                aux+=1
            else:     
                col = line.split(' ') 
                if(len(col)>13): col = col[1:]
                #print(col[1])
                if(int(col[2])==exp):
                    if m_i:
                        n_action.append(col[1])
                        actions.append(int(col[3])+250)
                        mot.append(col[4])
                        
                    else: 

                        n_action.append(col[1])
                        actions.append(int(col[3]))
                        if debug: print(col)
                        mot.append(col[4])
                        
                    if col[1] == "am0":
                        am0.append(1)
                    else: am0.append(0)  

                    if col[1] == "am1":
                        am1.append(1)
                    else: am1.append(0)  

                    if col[1] == "am2":
                        am2.append(1)
                    else: am2.append(0)  

                    if col[1] == "am3":
                        am3.append(1)
                    else: am3.append(0)  

                    if col[1] == "am4":
                        am4.append(1)
                    else: am4.append(0)  

                    if col[1] == "am5":
                        am5.append(1)
                    else: am5.append(0)  

                    if col[1] == "am6":
                        am6.append(1)
                    else: am6.append(0)  

                    if col[1] == "am7":
                        am7.append(1)
                    else: am7.append(0)  

                    if col[1] == "am8":
                        am8.append(1)
                    else: am8.append(0)  

                    if col[1] == "am9":
                        am9.append(1)
                    else: am9.append(0)  

                    if col[1] == "am10":
                        am10.append(1)
                    else: am10.append(0)  

                    if col[1] == "am11":
                        am11.append(1)
                    else: am11.append(0)  

                    if col[1] == "am12":
                        am12.append(1)
                    else: am12.append(0)  

                    if col[1] == "am13":
                        am13.append(1)
                    else: am13.append(0)  

                    if col[1] == "am14":
                        am14.append(1)
                    else: am14.append(0)  

                    if col[1] == "am15":
                        am15.append(1)
                    else: am15.append(0)  

                    if col[1] == "am16":
                        am16.append(1)
                    else: am16.append(0)    

                    if col[1] == "aa0":
                        aa0.append(1)
                    else: aa0.append(0)    

                    if col[1] == "aa1":
                        aa1.append(1)
                    else: aa1.append(0)    

                    if col[1] == "aa2":
                        aa2.append(1)
                    else: aa2.append(0)     
                    
                    exps.append(i)

                    i+=1
    actions = [am0, am1, am2, am3, am4, am5, am6, am7, am8, am9, am10, am11, am12, am13, am14, am15, am16, aa0, aa1, aa2] 
    return [n_action, exps, actions, mot, actions] # len 13


remove_strings_from_file(file1, strings_to_remove)
remove_strings_from_file(file2, strings_to_remove)


def plot_graphs_mot(title, mot1, hung1, cur1, mot2, r2, g2, b2, exp, max_ticks, step_ticks):
    cut = 3
    #Y_ticks = [i/10 for i in range(0,10, 1)]
    Y_ticks_act = [i for i in range(0,max_ticks, step_ticks)]

    plt.figure(figsize=(25,20))
    x = []
    for i, item in enumerate(r2[0]):
        x.append(i)
        if(type(item)==str): print(f"STRING: {i}")
    fig, ax1 = plt.subplots(figsize=(25, 20))
    #color = 'tab:blue'
    ax1.set_xlabel('Experiment')
    ax1.set_yticks(Y_ticks_act)
    ax1.tick_params(axis='y') # , labelcolor=color
    ax1.set_ylabel(title+"_Impulses")  # we already handled the x-label with ax1

    ax1.plot(x, r2[0], '^r:', label="R - Impulses") #color=color
    ax1.plot(x, g2[0], '^g:', label="G - Impulses") #color=color
    ax1.plot(x, b2[0], '^b:', label="B - Impulses") #color=color
    ax1.set_ylim([0, max_ticks])
    ax1.legend(loc="upper left")
    plt.savefig(output_folder+title+'_Impulses_Ac.pdf')      

    fig, ax1 = plt.subplots(figsize=(25, 20))
    #color = 'tab:blue'
    x = []
    for i, item in enumerate(hung1[0]):
        x.append(i)
        if(type(item)==str): print(f"STRING: {i}")

    ax1.set_xlabel('Experiment')
    ax1.set_yticks(Y_ticks_act)
    ax1.tick_params(axis='y') # , labelcolor=color
    ax1.set_ylabel(title+"_Drives")  # we already handled the x-label with ax1
    ax1.plot(x, cur1[0], 'sc:', label="Curiosity - Drives") #color=color
    ax1.plot(x, hung1[0], 'sm:', label="Survival - Drives") #color=color
    ax1.set_ylim([0, max_ticks])

    
    ax1.legend(loc="upper left")
    plt.savefig(output_folder+title+'_Drives_Ac.pdf')
    plt.close()  


def plot_actions(title, array):
    
    # Preparar os dados para plotagem

    actions = ["am0", "am1", "am2", "am3", "am4", "am5", "am6", "am7",
               "am8", "am9", "am10", "am11", "am12", "am13", "am14", 
               "am15", "am16", "aa0", "aa1", "aa2"]
    #print(f"len: "+str(len(array)))
    for j, action in enumerate(array[len(array)-1]):
        #print(j)
        x = []
        y = []
        for i, item in enumerate(action):
            x.append(i)
            #if(i!=len(action)-1): x.append(i+1)
            y.append(item)
            #if(i!=len(action)-1): y.append(item)
        #print("x")
        #print(x)
        #print("y")
        #print(y)
        
        plt.figure(figsize=(15,10))

        fig, ax1 = plt.subplots(figsize=(15, 10))

        ax1.set_xlabel('Step')
        ax1.tick_params(axis='y') # , labelcolor=color
        ax1.set_ylabel("Actions_"+title+"_"+actions[j])  # we already handled the x-label with ax1

        #ax1.plot(x, y, '^r-', label=actions[j]) #color=color
        ax1.bar(x, y, width = 1.5, color='r') #color=color

         # Adicionar título e rótulos dos eixos
        plt.title('Actions')
        plt.savefig(output_folder+title+"_"+actions[j]+'.pdf') 
        plt.close()



def plot_gantt(n_action1, n_action2, exp1, exp2, mot1, mot2,max_ticks, step_ticks):
    Y_ticks = [i for i in range(0,max_ticks, step_ticks)]

    actions = ["am0", "am1", "am2", "am3", "am4", "am5", "am6", "am7", "am8", "am9", "am10", "am11", "am12", 
               "am13", "am14", "am15", "am16", "aa0", "aa1", "aa2"] 
    colors = ['tab:blue', 'tab:red', 'tab:green', 'tab:orange']
    plt.figure(figsize=(25,15))

    fig, ax1 = plt.subplots(nrows=1, ncols=2, figsize=(25, 15), sharex=True)
    plt.tight_layout()

    #plt.subplot(321)
    
    ax1[0,0].set_yticks(Y_ticks)
    ax1[0,0].tick_params(axis='y') # , labelcolor=color
    ax1[0,0].set_xlabel('Step')
    ax1[0,0].tick_params(axis='y') # , labelcolor=color
    ax1[0,0].set_ylabel("Gantt_Chart_Actions_1_Q_Table")  # we already handled the x-label with ax1    

    # Agrupando ações iguais
    steps_agrupados = {}
    for acao in actions:
        for i, step in enumerate(n_action1):
            if step == acao:
                if acao in steps_agrupados: 
                    steps_agrupados[acao].extend([exp1[i]])
                else:
                    steps_agrupados[acao] = [exp1[i]]

    if debug: print(steps_agrupados)
# Plotando as barras horizontais
    for i, step_group in enumerate(steps_agrupados.values()):
        for step in step_group:
            ax1[0,0].broken_barh([(int(step), 1)], (i - 0.4, 0.8), edgecolor='black', cmap=mot1) #facecolors=colors[i%4]

    # Configurando o eixo y com as ações agrupadas
    ax1[0,0].set_yticks(range(len(actions)))
    ax1[0,0].set_yticklabels(actions)

        # Configurando o eixo x2
    ax1[0,0].set_xticks(exp1)
    ax1[0,0].set_xticklabels(exp1)  
    ax1[0,0].legend(loc="upper left")
    

    ### DRIVES ### 

    ax1[0,1].set_yticks(Y_ticks)
    ax1[0,1].tick_params(axis='y') # , labelcolor=color
    
    ax1[0,1].set_ylabel("Gantt_Chart_Actions_2 Q-Tables")  # we already handled the x-label with ax1    
     # Agrupando ações iguais
    steps_agrupados = {}
    for acao in actions:
        for i, step in enumerate(n_action2):
            if step == acao:
                if acao in steps_agrupados: 
                    steps_agrupados[acao].extend([exp2[i]])
                else:
                    steps_agrupados[acao] = [exp2[i]]

    if debug: print(steps_agrupados)
# Plotando as barras horizontais
    for i, step_group in enumerate(steps_agrupados.values()):
        for step in step_group:
            ax1[0,1].broken_barh([(int(step), 1)], (i - 0.4, 0.8), edgecolor='black', facecolors=colors[i%4])

    # Configurando o eixo y com as ações agrupadas
    ax1[0,1].set_yticks(range(len(actions)))
    ax1[0,1].set_yticklabels(actions)

        # Configurando o eixo x
    ax1[0,1].set_xticks(exp2)
    ax1[0,1].set_xticklabels(exp2)        
    ax1[0,1].legend(loc="upper left")
    plt.savefig(output_folder+'Gantt_Chart_Actions.pdf')      
    plt.close()
       
   
# Main
## Get data


results1 = get_data_drives(file1,93)
results2 = get_data_drives(file2,1)

#results1 = replace(results1)
#results2 = replace(results2)

if debug:
    #print(len(results1))
    print(len(results2))

#plot_actions("Impulses", results1)
plot_actions("1 Q-Table", results1)

plot_actions("2 Q-Tables", results2)


        