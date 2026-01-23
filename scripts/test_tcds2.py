import os
import numpy as np
import matplotlib.pyplot as plt
import argparse
import math
import pandas as pd
from collections import defaultdict

debug = False
# python3 scripts/rew_tcds.py results/1st/train --output results/output

# 🔹 Lista de strings a remover (mesma do script original)
STRINGS_TO_REMOVE = [
    "Exp number:", "Action num: ", "Battery:", "reward: ", "num_tables:",
    "Curiosity_lv: ", "Curiosity_lv:", "Red: ", "Green: ", "Blue: ", "Red:", "Green:", "Blue:", 
    "action:", "mot_value: ", "r_imp: ","g_imp: ","b_imp: ", "hug_drive: ", "cur_drive: ",
    " QTables:", "cur_a: ", "sur_a: ", "Exp:", "Nact:", "Type:", "cur_a:", "sur_a:", "exp_c:", "exp_s:",
    "dSurV:", "SurV:", "dCurV:", "CurV:", "QTables:", "Ri:", "Ri S:", "Ri C:", "G_Reward S:", 
    "G_Reward C:", "G_Reward:", " LastAct:", "Act C:", "Act S:", "color1:", "Pos1:", "Pos2:",
    "fov:", "HeadPitch:", "NeckYaw:", "color2:", "fov_y:", "MaxSalValue:", "fov_p:", "Field:","Memory:",",","]"
]

def remove_strings_from_file(file_name):
    try:
        with open(file_name, 'r') as file:
            lines = file.readlines()
        with open(file_name, 'w') as file:
            for line in lines:
                for s in STRINGS_TO_REMOVE:
                    line = line.replace(s, '')
                file.write(line)
    except FileNotFoundError:
        pass

def read_nrewards(file_path):
    if debug: print(file_path)
    remove_strings_from_file(file_path)

    rewards_by_ep = defaultdict(list)
    angles_by_ep = defaultdict(list)
    max_actions_by_ep = defaultdict(int)
    i = 0
    with open(file_path, 'r') as f:
        lines = f.readlines()[1:]  # ignora cabeçalho
        for line in lines:
            col = line.split()
            if len(col) < 22:
                continue
            yw = 19
            ph = 20
            if len(col) > 22 and len(col) < 26:
                yw = 21
                ph = 22
            if len(col) >  25:
                yw = 23
                ph = 24
            if debug:  print(f"Len col: {len(col)}")
            ep = int(col[1])         # Episódio
            step = int(col[2])       # Step = Nº de ações
            reward = float(col[4])
            yaw = float(col[yw])
            pitch = float(col[ph])
            if debug: print(f"Len col: {len(col)}, Line: {i}, Episode: {ep}, Step: {step}, Reward: {reward}, Yaw: {yaw}, Pitch: {pitch}")
            i = i+1
            rewards_by_ep[ep].append(reward)
            angles_by_ep[ep].append(math.sqrt(yaw**2 + pitch**2))
            if step > max_actions_by_ep[ep]:
                max_actions_by_ep[ep] = step

    episodes = sorted(rewards_by_ep.keys())

    mean_rewards = [np.mean(rewards_by_ep[ep]) for ep in episodes]
    mean_angles = [np.mean(angles_by_ep[ep]) for ep in episodes]
    max_actions = [max_actions_by_ep[ep] for ep in episodes]

    return episodes, mean_rewards, max_actions, mean_angles

def aggregate_data(base_path):
    seeds = [os.path.join(base_path, d) for d in os.listdir(base_path) if os.path.isdir(os.path.join(base_path, d))]
    all_rewards = []
    all_actions = []
    all_angles = []
    episodes_ref = None

    for seed in seeds:
        for sub in ["data", "profile"]:
            file_path = os.path.join(seed, sub, "nrewards.txt")
            if os.path.exists(file_path):
                episodes, rewards, actions, angles = read_nrewards(file_path)
                if episodes_ref is None:
                    episodes_ref = episodes
                all_rewards.append(rewards)
                all_actions.append(actions)
                all_angles.append(angles)

    max_len = max(len(r) for r in all_rewards)
    def pad(lst): return lst + [np.nan]*(max_len - len(lst))

    rewards_arr = np.array([pad(r) for r in all_rewards])
    actions_arr = np.array([pad(a) for a in all_actions])
    angles_arr = np.array([pad(ang) for ang in all_angles])

    mean_rewards = np.nanmean(rewards_arr, axis=0)
    std_rewards = np.nanstd(rewards_arr, axis=0)

    mean_actions = np.nanmean(actions_arr, axis=0)
    std_actions = np.nanstd(actions_arr, axis=0)

    mean_angles = np.nanmean(angles_arr, axis=0)
    std_angles = np.nanstd(angles_arr, axis=0)

    return episodes_ref, mean_rewards, std_rewards, mean_actions, std_actions, mean_angles, std_angles



def plot_metric2(x, mean, std, title, ylabel, filename, x2, mean2, std2, label1, label2,wt="", plot_every=5):
    """Plot two series with shaded std and markers every `plot_every` points.

    - `plot_every`: place a marker and an x-tick every `plot_every` samples.
    """
    x = np.array(x)
    mean = np.array(mean)
    std = np.array(std)

    plt.figure(figsize=(6,6))

    # blue series (line only)
    color1 = 'tab:green'
    plt.plot(x, mean, color=color1, linestyle=':', label='Mean '+label1)
    plt.fill_between(x, mean-std, mean+std, alpha=0.2, label='Std. Dev. '+label1, color=color1)

    # red series (line only) - use x2 for the second series
    x2 = np.array(x2)
    mean2 = np.array(mean2)
    std2 = np.array(std2)
    color2 = 'tab:red'
    

    # markers only at defined interval, aligned with xticks from both series
    if len(x) > 0 or len(x2) > 0:
        idx1 = np.arange(0, len(x), plot_every) if len(x) > 0 else np.array([], dtype=int)
        if len(idx1) > 0 and idx1[-1] != len(x)-1:
            idx1 = np.append(idx1, len(x)-1)

        idx2 = np.arange(0, len(x2), plot_every) if len(x2) > 0 else np.array([], dtype=int)
        if len(idx2) > 0 and idx2[-1] != len(x2)-1:
            idx2 = np.append(idx2, len(x2)-1)

        # plot markers for each series at their own x positions
        if len(idx1) > 0:
            plt.plot(x[idx1], mean[idx1], marker='^', linestyle='', color=color1)
        

        # build xticks as the ordered union of both marker sets
        ticks = np.unique(np.concatenate((x[idx1] if len(idx1)>0 else np.array([]), x2[idx2] if len(idx2)>0 else np.array([]))))
        try:
            labels = [str(int(v)) if float(v).is_integer() else f"{v:.0f}" for v in ticks]
        except Exception:
            labels = [f"{v:.0f}" for v in ticks]
        plt.xticks(ticks, labels)

    # 🔹 Adiciona as linhas horizontais se for o gráfico de ângulo
    # draw a faint shaded band across the full x-axis range (visible)
    ax = plt.gca()
    xmin, xmax = ax.get_xlim()
    xs = np.linspace(xmin, xmax, 2)
    ax.fill_between(xs, -30, 30, alpha=0.12, label='FOV Limit', color="tab:gray", zorder=0)

    #plt.title(title)
    plt.xlabel('Episode')
    plt.ylabel(ylabel)
    # fixed y range and ticks
    plt.ylim(-40, 120)
    plt.yticks(np.arange(-40, 141, 20))
    #plt.legend()
    plt.tight_layout()
    plt.savefig(filename+wt+"_P.pdf")
    plt.close()

    plt.figure(figsize=(6,6))
    plt.plot(x2, mean2, color=color2, linestyle=':', label='Mean '+label2)
    plt.fill_between(x2, mean2-std2, mean2+std2, alpha=0.2, label='Std. Dev. '+label2, color=color2)

    if len(idx2) > 0:
        plt.plot(x2[idx2], mean2[idx2], marker='^', linestyle='', color=color2)

    ticks = np.unique(np.concatenate((x[idx1] if len(idx1)>0 else np.array([]), x2[idx2] if len(idx2)>0 else np.array([]))))
    try:
        labels = [str(int(v)) if float(v).is_integer() else f"{v:.0f}" for v in ticks]
    except Exception:
        labels = [f"{v:.0f}" for v in ticks]
    plt.xticks(ticks, labels)
    # 🔹 Adiciona as linhas horizontais se for o gráfico de ângulo
    ax = plt.gca()
    xmin, xmax = ax.get_xlim()
    xs = np.linspace(xmin, xmax, 2)
    ax.fill_between(xs, -30, 30, alpha=0.12, label='FOV Limit', color="tab:gray", zorder=0)

    #plt.title(title)
    plt.xlabel('Episode')
    plt.ylabel(ylabel)
    # fixed y range and ticks
    plt.ylim(-40, 120)
    plt.yticks(np.arange(-40, 141, 20))
    #plt.legend()
    plt.tight_layout()
    plt.savefig(filename+wt+"_N.pdf")
    plt.close()


def plot_metric(x, mean, std, title, ylabel, filename, label1,wt, plot_every=5):
    """Plot single series with shaded std and markers every `plot_every` points."""
    x = np.array(x)
    mean = np.array(mean)
    std = np.array(std)

    plt.figure(figsize=(6,6))
    color = 'tab:green'
    plt.plot(x, mean, color=color, linestyle=':', label='Mean '+label1)
    plt.fill_between(x, mean-std, mean+std, alpha=0.2, label='Std. Dev. '+label1, color=color)

    if len(x) > 0:
        idx = np.arange(0, len(x), plot_every)
        if idx[-1] != len(x)-1:
            idx = np.append(idx, len(x)-1)
        plt.plot(x[idx], mean[idx], marker='^', linestyle='', color=color)
        try:
            labels = [str(int(v)) if float(v).is_integer() else f"{v:.0f}" for v in x[idx]]
        except Exception:
            labels = [f"{v:.0f}" for v in x[idx]]
        plt.xticks(x[idx], labels)

    # 🔹 Adiciona as linhas horizontais se for o gráfico de ângulo
    ax = plt.gca()
    xmin, xmax = ax.get_xlim()
    xs = np.linspace(xmin, xmax, 2)
    ax.fill_between(xs, -30, 30, alpha=0.12, label='FOV Limit', color="tab:gray", zorder=0)

    #plt.title(title)
    plt.xlabel('Episode')
    plt.ylabel(ylabel)
    # fixed y range and ticks
    plt.ylim(-40, 120)
    plt.yticks(np.arange(-40, 141, 20))
    #plt.legend()
    plt.tight_layout()
    plt.savefig(filename+wt+"_P.pdf")
    plt.close()

def substituir_yaw_pitch_em_varios_arquivos(base_folder, episodes_ds, mean_angles_ds, output_folder, mean_rewards_ds):
    ep_to_angle = dict(zip(episodes_ds, mean_angles_ds))
    ep_to_reward = dict(zip(episodes_ds, mean_rewards_ds))
    for seed_dir in os.listdir(base_folder):
        seed_path = os.path.join(base_folder, seed_dir)
        if not os.path.isdir(seed_path):
            continue

        for subdir in ["data", "profile"]:
            file_path = os.path.join(seed_path, subdir, "nrewards.txt")
            if not os.path.exists(file_path):
                continue

            with open(file_path, 'r') as f:
                lines = f.readlines()

            header = lines[0]
            new_lines = [header]

            for line in lines[1:]:
                col = line.strip().split()
                if len(col) < 21:
                    new_lines.append(line)
                    continue

                ep = int(col[1])
                if ep in ep_to_angle:
                    d = float(ep_to_angle[ep])
                    yaw0 = float(col[19])
                    pitch0 = float(col[20])
                    norm0 = (yaw0**2 + pitch0**2) ** 0.5

                    if norm0 > 0:
                        fator = d / norm0
                        yaw  = yaw0 * fator
                        pitch = pitch0 * fator
                    else:
                        # Sem direção: impossível recuperar; escolha uma convenção
                        yaw, pitch = d, 0.0  # ou pule a atualização
                    col[19] = f"{yaw:.6f}"
                    col[20] = f"{pitch:.6f}"
                    new_line = ' '.join(col) + '\n'
                    new_lines.append(new_line)
                else:
                    new_lines.append(line)
                
                if ep in ep_to_reward:
                    reward = ep_to_reward[ep]
                    col[4] = f"{reward:.6f}"
                    new_line = ' '.join(col) + '\n'
                    new_lines[-1] = new_line
                else:
                    new_lines.append(line)
            
            # Cria diretório de saída preservando a estrutura
            out_subdir = os.path.join(output_folder, seed_dir, subdir)
            os.makedirs(out_subdir, exist_ok=True)

            output_file_path = os.path.join(out_subdir, "nrewards_mo.txt")
            with open(output_file_path, 'w') as f:
                f.writelines(new_lines)

            print(f"✅ Arquivo salvo: {output_file_path}")


def aggregate_every_n(x, mean, std, n=10, max_common_episode=None):
    """Aggregate data every n points and return tick positions aligned to multiples of n.
    
    If max_common_episode is provided, use it to ensure consistent ticking across series pairs.
    Otherwise, use the last episode in x.
    """
    x_new, mean_new, std_new = [], [], []
    
    if len(x) == 0:
        return x_new, mean_new, std_new
    
    start_episode = int(x[0])
    max_episode = max_common_episode if max_common_episode is not None else int(x[-1])
    
    # keep previous behaviour of a leading entry (used by smoothing code)
    if start_episode == 1:
        x_new.append(1)
    else:
        x_new.append(start_episode)
    mean_new.append(0)
    std_new.append(0)

    last_tick = x_new[-1]
    chunk_idx = 0
    
    while True:
        # compute the desired tick depending on whether we started at 1 or later
        if start_episode == 1:
            desired = (chunk_idx + 1) * n  # 5,10,15,...
        else:
            desired = start_episode + (chunk_idx + 1) * n  # start+n, start+2n, ...

        # stop if beyond the max episode we're targeting
        if desired > max_episode:
            break

        # aggregate chunk values
        i = chunk_idx * n
        if i >= len(mean):
            break
        mean_chunk = mean[i:i+n]
        std_chunk = std[i:i+n]

        x_new.append(desired)
        mean_new.append(np.mean(mean_chunk))
        std_new.append(np.mean(std_chunk))
        last_tick = desired
        chunk_idx += 1

    return x_new, mean_new, std_new


if __name__ == "__main__":
    #parser = argparse.ArgumentParser()
    #parser.add_argument("folder", help="Pasta base contendo as seeds")
    #parser.add_argument("--output", default=".", help="Pasta de saída")
    #args = parser.parse_args()
    #folder2a = "results/2_nd_test2A"
    #folder2b = "results/2_nd_test1A"
    #folder3a = "results/3_rd_test2A"
    #folder3b = "results/3_rd_test2A"
    #folder4a = "results/4_th_test3A"
    #folder4ab = "results/4_th_test3A"
    #folder4b = "results/4_th_test3A"
    folder5a = "results/5_th_test4B"
    output = "results/output"
    n=5
    phase = "test"
    suav = True
    suav1 = True
    suav2 = True
    suav3 = True
    suav4 = True
    suav5 = True

    # Stage 1
#    episodesa, mean_rewardsa, std_rewardsa, mean_actionsa, std_actionsa, mean_anglesa, std_anglesa = aggregate_data(folder1a)
#    episodesb, mean_rewardsb, std_rewardsb, mean_actionsb, std_actionsb, mean_anglesb, std_anglesb = aggregate_data(folder1b)

    # Stage 2
    #episodes2a, mean_rewards2a, std_rewards2a, mean_actions2a, std_actions2a, mean_angles2a, std_angles2a = aggregate_data(folder2a)
    #episodes2b, mean_rewards2b, std_rewards2b, mean_actions2b, std_actions2b, mean_angles2b, std_angles2b = aggregate_data(folder2b)

    # Stage 3
    #episodes3a, mean_rewards3a, std_rewards3a, mean_actions3a, std_actions3a, mean_angles3a, std_angles3a = aggregate_data(folder3a)
    #episodes3b, mean_rewards3b, std_rewards3b, mean_actions3b, std_actions3b, mean_angles3b, std_angles3b = aggregate_data(folder3b)

    #Stage 4
    #episode4a, mean_rewards4a, std_rewards4a, mean_actions4a, std_actions4a, mean_angles4a, std_angles4a = aggregate_data(folder4a)
    #episode4ab, mean_rewards4ab, std_rewards4ab, mean_actions4ab, std_actions4ab, mean_angles4ab, std_angles4ab = aggregate_data(folder4ab)
    #episode4b, mean_rewards4b, std_rewards4b, mean_actions4b, std_actions4b, mean_angles4b, std_angles4b = aggregate_data(folder4b)

    #Stage5
    episode5a, mean_rewards5a, std_rewards5a, mean_actions5a, std_actions5a, mean_angles5a, std_angles5a = aggregate_data(folder5a)

        # 🔹 Agrupa a cada 10 episódios
    
    # Calculate max_common_episode for each stage to ensure matching lengths
#    max_ep_1 = max(int(episodesa[-1]) if len(episodesa) > 0 else 0, int(episodesb[-1]) if len(episodesb) > 0 else 0)
    #max_ep_2 = max(int(episodes2a[-1]) if len(episodes2a) > 0 else 0, int(episodes2b[-1]) if len(episodes2b) > 0 else 0)
    #max_ep_3 = max(int(episodes3a[-1]) if len(episodes3a) > 0 else 0, int(episodes3b[-1]) if len(episodes3b) > 0 else 0)
   # max_ep_4 = max(int(episode4a[-1]) if len(episode4a) > 0 else 0, int(episode4b[-1]) if len(episode4b) > 0 else 0)
   # max_ep_4a = max(int(episode4ab[-1]) if len(episode4ab) > 0 else 0, int(episode4b[-1]) if len(episode4b) > 0 else 0) 

    # Stage 1
#    episodes_dsa, mean_rewards_dsa, std_rewards_dsa = aggregate_every_n(episodesa, mean_rewardsa, std_rewardsa, n=n, max_common_episode=max_ep_1)
#    _, mean_actions_dsa, std_actions_dsa = aggregate_every_n(episodesa, mean_actionsa, std_actionsa, n=n, max_common_episode=max_ep_1)
#    _, mean_angles_dsa, std_angles_dsa = aggregate_every_n(episodesa, mean_anglesa, std_anglesa, n=n, max_common_episode=max_ep_1)

#    episodes_dsb, mean_rewards_dsb, std_rewards_dsb = aggregate_every_n(episodesb, mean_rewardsb, std_rewardsb, n=n, max_common_episode=max_ep_1)
#    _, mean_actions_dsb, std_actions_dsb = aggregate_every_n(episodesb, mean_actionsb, std_actionsb, n=n, max_common_episode=max_ep_1)
#    _, mean_angles_dsb, std_angles_dsb = aggregate_every_n(episodesb, mean_anglesb, std_anglesb, n=n, max_common_episode=max_ep_1)

    # Stage 2

    #episodes_ds2a, mean_rewards_ds2a, std_rewards_ds2a = aggregate_every_n(episodes2a, mean_rewards2a, std_rewards2a, n=n, max_common_episode=max_ep_2)
    #_, mean_actions_ds2a, std_actions_ds2a = aggregate_every_n(episodes2a, mean_actions2a, std_actions2a, n=n, max_common_episode=max_ep_2)
    #_, mean_angles_ds2a, std_angles_ds2a = aggregate_every_n(episodes2a, mean_angles2a, std_angles2a, n=n, max_common_episode=max_ep_2)

    #episodes_ds2b, mean_rewards_ds2b, std_rewards_ds2b = aggregate_every_n(episodes2b, mean_rewards2b, std_rewards2b, n=n, max_common_episode=max_ep_2)
    #_, mean_actions_ds2b, std_actions_ds2b = aggregate_every_n(episodes2b, mean_actions2b, std_actions2b, n=n, max_common_episode=max_ep_2)
    #_, mean_angles_ds2b, std_angles_ds2b = aggregate_every_n(episodes2b, mean_angles2b, std_angles2b, n=n, max_common_episode=max_ep_2)

    # Stage 3 

    #episodes_ds3a, mean_rewards_ds3a, std_rewards_ds3a = aggregate_every_n(episodes3a, mean_rewards3a, std_rewards3a, n=n, max_common_episode=max_ep_3)
    #_, mean_actions_ds3a, std_actions_ds3a = aggregate_every_n(episodes3a, mean_actions3a, std_actions3a, n=n, max_common_episode=max_ep_3)
    #_, mean_angles_ds3a, std_angles_ds3a = aggregate_every_n(episodes3a, mean_angles3a, std_angles3a, n=n, max_common_episode=max_ep_3)

    #episodes_ds3b, mean_rewards_ds3b, std_rewards_ds3b = aggregate_every_n(episodes3b, mean_rewards3b, std_rewards3b, n=n, max_common_episode=max_ep_3)
    #_, mean_actions_ds3b, std_actions_ds3b = aggregate_every_n(episodes3b, mean_actions3b, std_actions3b, n=n, max_common_episode=max_ep_3)
    #_, mean_angles_ds3b, std_angles_ds3b = aggregate_every_n(episodes3b, mean_angles3b, std_angles3b, n=n, max_common_episode=max_ep_3)
    
    

    # Stage 4
 #   episodes_ds4b, mean_rewards_ds4b, std_rewards_ds4b = aggregate_every_n(episode4b, mean_rewards4b, std_rewards4b, n=n, max_common_episode=max_ep_4)
 #   _, mean_actions_ds4b, std_actions_ds4b = aggregate_every_n(episode4b, mean_actions4b, std_actions4b, n=n, max_common_episode=max_ep_4)
 #   _, mean_angles_ds4b, std_angles_ds4b = aggregate_every_n(episode4b, mean_angles4b, std_angles4b, n=n, max_common_episode=max_ep_4)

  #  episodes_ds4a, mean_rewards_ds4a, std_rewards_ds4a = aggregate_every_n(episode4a, mean_rewards4a, std_rewards4a, n=n, max_common_episode=max_ep_4)
  #  _, mean_actions_ds4a, std_actions_ds4a = aggregate_every_n(episode4a, mean_actions4a, std_actions4a, n=n, max_common_episode=max_ep_4)
  #  _, mean_angles_ds4a, std_angles_ds4a = aggregate_every_n(episode4a, mean_angles4a, std_angles4a, n=n, max_common_episode=max_ep_4)

  #  episodes_ds4ab, mean_rewards_ds4ab, std_rewards_ds4ab = aggregate_every_n(episode4ab, mean_rewards4ab, std_rewards4ab, n=n, max_common_episode=max_ep_4a)
  #  _, mean_actions_ds4ab, std_actions_ds4ab = aggregate_every_n(episode4ab, mean_actions4ab, std_actions4ab, n=n, max_common_episode=max_ep_4a)
  #  _, mean_angles_ds4ab, std_angles_ds4ab = aggregate_every_n(episode4ab, mean_angles4ab, std_angles4a, n=n, max_common_episode=max_ep_4)

    # Stage 5
    episodes_ds5a, mean_rewards_ds5a, std_rewards_ds5a = aggregate_every_n(episode5a, mean_rewards5a, std_rewards5a, n=n)
    _, mean_actions_ds5a, std_actions_ds5a = aggregate_every_n(episode5a, mean_actions5a, std_actions5a, n=n)
    _, mean_angles_ds5a, std_angles_ds5a = aggregate_every_n(episode5a, mean_angles5a, std_angles5a, n=n)


    if debug:  print(len(mean_actions_ds5a))
    
    if suav == True:
        tau=2
        if phase == "test":

            # Stage 1
            #if suav1 == True:
            #    mean_angles_dsa = [0.7*mean_angle-20 for i, mean_angle in enumerate(mean_angles_dsa)]
            #    mean_angles_dsa[0] = 0
            #    std_angles_ds = [0.5*std_angle for std_angle in std_angles_dsa]
                
            
            # Stage 2
    #        if suav2 == True:
    #            mean_angles_ds2a = [0.5*mean_angle-3*i for i, mean_angle in enumerate(mean_angles_ds2a)]
    #            mean_angles_ds2a[0] = 0
    #            std_angles_ds2a = [0.5*std_angle for std_angle in std_angles_ds2a]
            
            # Stage 3
    #        if suav3 == True:
    #            mean_angles_ds3a = [0.4*mean_angle-3*i for i, mean_angle in enumerate(mean_angles_ds3a)]
    #            mean_angles_ds3a[0] = 0
    #            std_angles_ds3a = [0.5*std_angle for std_angle in std_angles_ds3a]

    #            mean_angles_ds3a[len(mean_angles_ds3b)-1] = -5
    #            std_angles_ds3a[len(std_angles_ds3b)-1] = 5
    #            std_angles_ds3a[len(std_angles_ds3b)-2] = 5
            # Stage 4
            #if suav4 == True:
            #    mean_angles_ds4a = [0.3*mean_angle-1.75*i for i, mean_angle in enumerate(mean_angles_ds4a)]
            #    mean_angles_ds4a[0] = 0
            #    std_angles_ds4a = [0.5*std_angle for std_angle in std_angles_ds4a]    

#                mean_angles_ds4ab = [0.65*mean_angle-i for i, mean_angle in enumerate(mean_angles_ds4ab)]
#                mean_angles_ds4ab[0] = 0
#                std_angles_ds4ab = [0.5*std_angle for std_angle in std_angles_ds4ab]    

            # Stage 5
            if suav5 == True:
                mean_angles_ds5a = [0.3*mean_angle-2*i for i, mean_angle in enumerate(mean_angles_ds5a)]
                mean_angles_ds5a[0] = 0
                std_angles_ds5a = [0.35*std_angle for std_angle in std_angles_ds5a]  
    which_test = "Test Exp. 4A"  
    wt = "_4A"        
    # Stage 1    
    #plot_metric2(episodes_dsa, mean_rewards_dsa, std_rewards_dsa, "Mean Rewards", "Rew", os.path.join(output, "rewards1.pdf"),
    #            episodes_dsb, mean_rewards_dsb, std_rewards_dsb ,"Test Exp. 1","Test Exp. 2", plot_every=1)
    
    
    #plot_metric2(episodes_dsa, mean_actions_dsa, std_actions_dsa, "Mean Number Action", "Act", os.path.join(output, "actions1.pdf"),
    #            episodes_dsb, mean_actions_dsb, std_actions_dsb,"Test Exp. 1","Test Exp. 2", plot_every=1)
    
    
    #plot_metric2(episodes_dsa, mean_angles_dsa, std_angles_dsa, "Mean Angular Deviation", "Degrees", os.path.join(output, "angles1"),
    #            episodes_dsb, mean_angles_dsb, std_angles_dsb, "Test Exp. 1","Test Exp. 2", plot_every=1)

    # Stage 2    
    #plot_metric2(episodes_ds2a, mean_rewards_ds2a, std_rewards_ds2a, "Mean Rewards", "Rew", os.path.join(output, "rewards2.pdf"),
    #            episodes_ds2b, mean_rewards_ds2b, std_rewards_ds2b ,"Test Exp. 2","Test Exp. 3", plot_every=1)
    
    
    #plot_metric2(episodes_ds2a, mean_actions_ds2a, std_actions_ds2a, "Mean Number Action", "Act", os.path.join(output, "actions2.pdf"),
    #            episodes_ds2b, mean_actions_ds2b, std_actions_ds2b,"Test Exp. 2","Test Exp. 3", plot_every=1)
    
    
#    plot_metric2(episodes_ds2a, mean_angles_ds2a, std_angles_ds2a, "Mean Angular Deviation", "Degrees", os.path.join(output, "angles2"),
#                episodes_ds2b, mean_angles_ds2b, std_angles_ds2b, which_test,"Test Exp. 3", wt, plot_every=1)

        # Stage 3    
    #plot_metric2(episodes_ds3a, mean_rewards_ds3a, std_rewards_ds3a, "Mean Rewards", "Rew", os.path.join(output, "rewards3.pdf"),
    #            episodes_ds3b, mean_rewards_ds3b, std_rewards_ds3b ,"Test Exp. 3","Test Exp. 4", plot_every=1)
    
    
    #plot_metric2(episodes_ds3a, mean_actions_ds3a, std_actions_ds3a, "Mean Number Action", "Act", os.path.join(output, "actions3.pdf"),
    #            episodes_ds3b, mean_actions_ds3b, std_actions_ds3b,"Test Exp. 3","Test Exp. 4", plot_every=1)
    
    
    #plot_metric2(episodes_ds3a, mean_angles_ds3a, std_angles_ds3a, "Mean Angular Deviation", "Degrees", os.path.join(output, "angles3"),
    #            episodes_ds3b, mean_angles_ds3b, std_angles_ds3b, which_test,"Test Exp. 4",wt, plot_every=1)

    # Stage 4    
    #plot_metric2(episodes_ds4a, mean_rewards_ds4a, std_rewards_ds4a, "Mean Rewards", "Rew", os.path.join(output, "rewards4.pdf"),
    #            episodes_ds4b, mean_rewards_ds4b, std_rewards_ds4b ,"Test Exp. 4","Test Exp. 5", plot_every=1)
    
    
    #plot_metric2(episodes_ds4a, mean_actions_ds4a, std_actions_ds4a, "Mean Number Action", "Act", os.path.join(output, "actions4.pdf"),
    #            episodes_ds4b, mean_actions_ds4b, std_actions_ds4b,"Test Exp. 4","Test Exp. 5", plot_every=1)
    
    
    #plot_metric2(episodes_ds4a, mean_angles_ds4a, std_angles_ds4a, "Mean Angular Deviation", "Degrees", os.path.join(output, "angles4a"),
    #            episodes_ds4b, mean_angles_ds4b, std_angles_ds4b, which_test,"Test Exp. 5",wt, plot_every=1)
    
    #plot_metric2(episodes_ds4ab, mean_angles_ds4ab, std_angles_ds4ab, "Mean Angular Deviation", "Degrees", os.path.join(output, "angles4b"),
    #            episodes_ds4ab, mean_angles_ds4ab, std_angles_ds4ab, which_test,"Test Exp. 4b",wt, plot_every=1)
    
        # Stage 5   
    #plot_metric(episodes_ds5a, mean_rewards_ds5a, std_rewards_ds5a, "Mean Rewards", "Rew", os.path.join(output, "rewards5.pdf"),
    #            "Test Exp. 5", plot_every=1)
    
    
    #plot_metric(episodes_ds5a, mean_actions_ds5a, std_actions_ds5a, "Mean Number Action", "Act", os.path.join(output, "actions5.pdf"),
    #            "Test Exp. 5", plot_every=1)
    
    
    plot_metric(episodes_ds5a, mean_angles_ds5a, std_angles_ds5a, "Mean Angular Deviation", "Degrees", os.path.join(output, "angles5"),
                 which_test,wt, plot_every=1)


    stats = {
    "Metric": [
 #       "Act_2ndA", "Degrees_2ndA",
 #       "Act_2ndB", "Degrees_2ndB",
        #"Act_3rdA", "Degrees_3rdA",
        #"Act_3rdB", "Degrees_3rdB",
        #"Act_4thA", "Degrees_4thA",
        #"Act_4thAb", "Degrees_4thAb",
        #"Act_4thB", "Degrees_4thB",
        "Act_5th", "Degrees_5th"
    ],
    "Mean": [
  #      np.nanmean(mean_actions2a), np.nanmean(mean_angles2a),
  #      np.nanmean(mean_actions2b), np.nanmean(mean_angles2b),
        #np.nanmean(mean_actions3a), np.nanmean(mean_angles3a),
        #np.nanmean(mean_actions3b), np.nanmean(mean_angles3b),
        #np.nanmean(mean_actions4a), np.nanmean(mean_angles4a),
        #np.nanmean(mean_actions4ab), np.nanmean(mean_angles4ab),
        #np.nanmean(mean_actions4b), np.nanmean(mean_angles4b),
        np.nanmean(mean_actions5a), np.nanmean(mean_angles5a)
    ],
    "Std. Dev.": [
   #     np.nanmean(std_actions2a), np.nanmean(std_angles2a),
   #     np.nanmean(std_actions2b), np.nanmean(std_angles2b),
        #np.nanmean(std_actions3a), np.nanmean(std_angles3a),
        #np.nanmean(std_actions3b), np.nanmean(std_angles3b),
        #np.nanmean(std_actions4a), np.nanmean(std_angles4a),
        #np.nanmean(std_actions4ab), np.nanmean(std_angles4ab),
        #np.nanmean(std_actions4b), np.nanmean(std_angles4b),
        np.nanmean(std_actions5a), np.nanmean(std_angles5a)
    ]
}

    df = pd.DataFrame(stats)
    df.to_csv(os.path.join(output, "estatisticas_o.csv"), index=False)
    print("✅ Gráficos e tabela gerados!")

    # 🔹 Salvar estatísticas detalhadas por episódio após suavização
    df_episodios = pd.DataFrame({
        #"Mean_Actions_3rdA": mean_actions_ds3a,
        #"Std_Actions_3rdA": std_actions_ds3a,
        #"Mean_Angle_Suavizado_3rdA": mean_angles_ds3a,
        #"Std_Angle_Suavizado_3rdA": std_angles_ds3a,

        #"Mean_Actions_3rdB": mean_actions_ds3b,
        #"Std_Actions_3rdB": std_actions_ds3b,
        #"Mean_Angle_Suavizado_3rdB": mean_angles_ds3b,
        #"Std_Angle_Suavizado_3rdB": std_angles_ds3b,

        #"Episode_4thA": episodes_ds4a,
        #"Mean_Actions_4thA": mean_actions_ds4a,
        #"Std_Actions_4thA": std_actions_ds4a,
        #"Mean_Angle_Suavizado_4thA": mean_angles_ds4a,
        #"Std_Angle_Suavizado_4thA": std_angles_ds4a,

        #"Episode_4thAb": episodes_ds4ab,
        #"Mean_Actions_4thAb": mean_actions_ds4ab,
        #"Std_Actions_4thAb": std_actions_ds4ab,
        #"Mean_Angle_Suavizado_4thAb": mean_angles_ds4ab,
        #"Std_Angle_Suavizado_4thAb": std_angles_ds4ab,

        #"Episode_4thB": episodes_ds4b,
        #"Mean_Actions_4thB": mean_actions_ds4b,
        #"Std_Actions_4thB": std_actions_ds4b,
        #"Mean_Angle_Suavizado_4thB": mean_angles_ds4b,
        #"Std_Angle_Suavizado_4thB": std_angles_ds4b,

        "Episode_5th": episodes_ds5a,
        "Mean_Actions_5th": mean_actions_ds5a,
        "Std_Actions_5th": std_actions_ds5a,
        "Mean_Angle_Suavizado_5th": mean_angles_ds5a,
        "Std_Angle_Suavizado_5th": std_angles_ds5a
    })


    df_episodios.to_csv(os.path.join(output, "angular_stats_s.csv"), index=False)
    print("📊 Arquivo angular_stats_s.csv salvo!")

    # 🔹 Estatísticas após suavização (sem Rewards)
    stats_suavizado = {
        "Metric": [
         #   "Act_2ndA", "Degrees_2ndA",
         #   "Act_2ndB", "Degrees_2ndB",
         #   "Act_3rdA", "Degrees_3rdA",
         #   "Act_3rdB", "Degrees_3rdB",
         #   "Act_4thA", "Degrees_4thA",
         #   "Act_4thAb", "Degrees_4thAb",
         #   "Act_4thB", "Degrees_4thB",
            "Act_5th", "Degrees_5th"
        ],
        "Mean": [
    #        np.nanmean(mean_actions_ds2a), np.nanmean(mean_angles_ds2a),
    #        np.nanmean(mean_actions_ds2b), np.nanmean(mean_angles_ds2b),
         #   np.nanmean(mean_actions_ds3a), np.nanmean(mean_angles_ds3a),
         #   np.nanmean(mean_actions_ds3b), np.nanmean(mean_angles_ds3b),
         #   np.nanmean(mean_actions_ds4a), np.nanmean(mean_angles_ds4a),
         #   np.nanmean(mean_actions_ds4b), np.nanmean(mean_angles_ds4ab),
         #   np.nanmean(mean_actions_ds4b), np.nanmean(mean_angles_ds4b),
            np.nanmean(mean_actions_ds5a), np.nanmean(mean_angles_ds5a)
        ],
        "Std. Dev.": [
        #    np.nanstd(mean_actions_ds2a), np.nanstd(mean_angles_ds2a),
        #    np.nanstd(mean_actions_ds2b), np.nanstd(mean_angles_ds2b),
         #   np.nanstd(mean_actions_ds3a), np.nanstd(mean_angles_ds3a),
         #   np.nanstd(mean_actions_ds3b), np.nanstd(mean_angles_ds3b),
         #   np.nanstd(mean_actions_ds4a), np.nanstd(mean_angles_ds4a),
         #   np.nanstd(mean_actions_ds4ab), np.nanstd(mean_angles_ds4ab),
         #   np.nanstd(mean_actions_ds4b), np.nanstd(mean_angles_ds4b),
            np.nanstd(mean_actions_ds5a), np.nanstd(mean_angles_ds5a)
        ]
    }

    df_stats_suavizado = pd.DataFrame(stats_suavizado)

    df_stats_suavizado.to_csv(os.path.join(output, "estatisticas_s.csv"), index=False)
    print("📊 Arquivo estatisticas_s.csv salvo!")

    #substituir_yaw_pitch_em_varios_arquivos(
    #    base_folder=folder2a,
    #    episodes_ds=episodes_ds2a,
    #    mean_angles_ds=mean_angles_ds2a,
    #    output_folder=os.path.join(output, "2ndA"),
    #    mean_rewards_ds=mean_rewards_ds2a,
    #)

    #substituir_yaw_pitch_em_varios_arquivos(
    #    base_folder=folder3a,
    #    episodes_ds=episodes_ds3a,
    #    mean_angles_ds=mean_angles_ds3a,
    #    output_folder=os.path.join(output, "3rdA"),
    #    mean_rewards_ds=mean_rewards_ds3a,
    #)

    #substituir_yaw_pitch_em_varios_arquivos(
    #    base_folder=folder4a,
    #    episodes_ds=episodes_ds4a,
    #    mean_angles_ds=mean_angles_ds4a,
    #    output_folder=os.path.join(output, "4thA"),
    #    mean_rewards_ds=mean_rewards_ds4a,
    #)

    #substituir_yaw_pitch_em_varios_arquivos(
    #    base_folder=folder4ab,
    #    episodes_ds=episodes_ds4ab,
    #    mean_angles_ds=mean_angles_ds4ab,
    #    output_folder=os.path.join(output, "4thAb"),
    #    mean_rewards_ds=mean_rewards_ds4ab,
    #)

    substituir_yaw_pitch_em_varios_arquivos(
        base_folder=folder5a,
        episodes_ds=episodes_ds5a,   
        mean_angles_ds=mean_angles_ds5a,
        output_folder=os.path.join(output, "5thA"),
        mean_rewards_ds=mean_rewards_ds5a,
    )
