#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import os
import math
from collections import defaultdict

import numpy as np
import pandas as pd

# (Opcional) SciPy para p-value/IC com distribuição t
try:
    import scipy.stats as scipy_stats
except Exception:
    scipy_stats = None
    print("⚠️ scipy.stats não disponível; p-value/IC ficarão como NaN (t/df ainda serão calculados).")


# Strings que aparecem no arquivo e atrapalham o split/parse (mantido do seu exemplo)
STRINGS_TO_REMOVE = [
    "Exp number:", "Action num: ", "Battery:", "reward: ", "num_tables:",
    "Curiosity_lv: ", "Curiosity_lv:", "Red: ", "Green: ", "Blue: ", "Red:", "Green:", "Blue:",
    "action:", "mot_value: ", "r_imp: ", "g_imp: ", "b_imp: ", "hug_drive: ", "cur_drive: ",
    " QTables:", "cur_a: ", "sur_a: ", "Exp:", "Nact:", "Type:", "cur_a:", "sur_a:", "exp_c:", "exp_s:",
    "dSurV:", "SurV:", "dCurV:", "CurV:", "QTables:", "Ri:", "Ri S:", "Ri C:", "G_Reward S:",
    "G_Reward C:", "G_Reward:", " LastAct:", "Act C:", "Act S:", "color1:", "Pos1:", "Pos2:",
    "fov:", "HeadPitch:", "NeckYaw:", "color2:", "fov_y:", "fov_p:", "Field:"
]


def _clean_line(line: str) -> str:
    """Remove strings conhecidas SEM modificar o arquivo original."""
    for s in STRINGS_TO_REMOVE:
        line = line.replace(s, "")
    return line


def read_rewards_mean_per_episode(nrewards_path: str):
    """
    Lê um nrewards.txt e retorna:
      - episodes (lista ordenada)
      - mean_rewards_per_episode (média do reward dentro de cada episódio)

    Observação: o arquivo tem várias linhas por episódio (steps). Aqui agregamos por episódio.
    Espera colunas compatíveis com o seu exemplo:
      col[1] = Episode
      col[4] = reward
    """
    rewards_by_ep = defaultdict(list)

    with open(nrewards_path, "r", encoding="utf-8", errors="ignore") as f:
        lines = f.readlines()

    # Pula header
    for raw in lines[1:]:
        line = _clean_line(raw).strip()
        if not line:
            continue

        col = line.split()
        if len(col) < 5:
            continue

        try:
            ep = int(col[1])
            r = float(col[4])
        except Exception:
            continue

        rewards_by_ep[ep].append(r)

    if not rewards_by_ep:
        return [], np.array([])

    episodes = sorted(rewards_by_ep.keys())
    mean_rewards = np.array([float(np.mean(rewards_by_ep[ep])) for ep in episodes], dtype=float)
    return episodes, mean_rewards


def list_seed_dirs(base_path: str):
    if not os.path.isdir(base_path):
        return []
    return [
        os.path.join(base_path, d)
        for d in os.listdir(base_path)
        if os.path.isdir(os.path.join(base_path, d))
    ]


def sample_rewards_per_seed_mean(base_path: str):
    """
    Amostra 1 (per_seed):
      para cada seed em results/<dataset>/<seed>/
      lê profile/nrewards.txt
      -> calcula a média das médias por episódio
      -> retorna um array com 1 valor por seed

    Isso tende a ser a amostragem mais “honesta” (seeds independentes).
    """
    out = []
    for seed_dir in list_seed_dirs(base_path):
        seed_values = []
        for sub in ("profile"):
            p = os.path.join(seed_dir, sub, "nrewards.txt")
            if not os.path.exists(p):
                continue
            _, mean_rewards = read_rewards_mean_per_episode(p)
            if mean_rewards.size:
                seed_values.append(float(np.nanmean(mean_rewards)))
        if seed_values:
            out.append(float(np.nanmean(seed_values)))

    return np.array(out, dtype=float)


def aggregate_mean_rewards_over_seeds(base_path: str):
    """
    Produz uma série agregada por episódio (média entre seeds):
      - Para cada seed, pega a série mean_reward_por_ep
      - Alinha por índice (episódio ordenado) via padding com NaN
      - Retorna mean_rewards_agg (shape [n_episodes])
    """
    series = []

    for seed_dir in list_seed_dirs(base_path):
        for sub in ("data", "profile"):
            p = os.path.join(seed_dir, sub, "nrewards_mo.txt")
            if not os.path.exists(p):
                continue
            _, mean_rewards = read_rewards_mean_per_episode(p)
            if mean_rewards.size:
                series.append(mean_rewards)

    if not series:
        return np.array([], dtype=float)

    max_len = max(len(s) for s in series)

    def pad(arr):
        if len(arr) == max_len:
            return arr
        return np.concatenate([arr, np.full(max_len - len(arr), np.nan, dtype=float)])

    mat = np.vstack([pad(s) for s in series])  # [n_runs, n_episodes]
    return np.nanmean(mat, axis=0)


def aggregate_every_n(values: np.ndarray, n: int = 10):
    """
    Agrupa valores em bins de tamanho n e tira a média de cada bin.
    Retorna um array de tamanho ~ ceil(len(values)/n).
    """
    values = np.asarray(values, dtype=float)
    values = values[~np.isnan(values)]
    if values.size == 0:
        return np.array([], dtype=float)

    bins = []
    for i in range(0, len(values), n):
        chunk = values[i:i + n]
        bins.append(float(np.mean(chunk)))
    return np.array(bins, dtype=float)


def cohen_d(a, b):
    a = np.asarray(a, dtype=float)
    b = np.asarray(b, dtype=float)
    a = a[~np.isnan(a)]
    b = b[~np.isnan(b)]
    if a.size < 2 or b.size < 2:
        return np.nan

    m1, m2 = float(np.mean(a)), float(np.mean(b))
    s1 = float(np.std(a, ddof=1))
    s2 = float(np.std(b, ddof=1))
    denom = math.sqrt((s1 * s1 + s2 * s2) / 2.0)
    if denom == 0:
        return np.nan
    return (m1 - m2) / denom


def welch_df(a, b):
    a = np.asarray(a, dtype=float)
    b = np.asarray(b, dtype=float)
    a = a[~np.isnan(a)]
    b = b[~np.isnan(b)]
    n1, n2 = a.size, b.size
    if n1 < 2 or n2 < 2:
        return np.nan

    v1 = float(np.var(a, ddof=1))
    v2 = float(np.var(b, ddof=1))
    num = (v1 / n1 + v2 / n2) ** 2
    den = (v1 * v1) / (n1 * n1 * (n1 - 1)) + (v2 * v2) / (n2 * n2 * (n2 - 1))
    return num / den if den != 0 else np.nan


def welch_ttest(a, b, alpha=0.05):
    """
    Welch t-test (amostras independentes, variâncias diferentes).
    Retorna dict com:
      n, mean, std, diff, t, df, p, CI95, d
    """
    a = np.asarray(a, dtype=float)
    b = np.asarray(b, dtype=float)
    a = a[~np.isnan(a)]
    b = b[~np.isnan(b)]

    res = {
        "n_A": int(a.size),
        "n_B": int(b.size),
        "mean_A": float(np.mean(a)) if a.size else np.nan,
        "std_A": float(np.std(a, ddof=1)) if a.size > 1 else np.nan,
        "mean_B": float(np.mean(b)) if b.size else np.nan,
        "std_B": float(np.std(b, ddof=1)) if b.size > 1 else np.nan,
    }
    res["mean_diff"] = res["mean_A"] - res["mean_B"]
    res["df"] = float(welch_df(a, b)) if a.size >= 2 and b.size >= 2 else np.nan
    res["cohen_d"] = float(cohen_d(a, b)) if a.size >= 2 and b.size >= 2 else np.nan

    if a.size < 2 or b.size < 2:
        res.update({"t_stat": np.nan, "p_value": np.nan, "ci95_low": np.nan, "ci95_high": np.nan})
        return res

    # t-stat manual (para funcionar mesmo sem SciPy)
    v1 = float(np.var(a, ddof=1))
    v2 = float(np.var(b, ddof=1))
    se = math.sqrt(v1 / a.size + v2 / b.size) if (a.size and b.size) else np.nan
    res["t_stat"] = float(res["mean_diff"] / se) if se and se != 0 else np.nan

    if scipy_stats is None or np.isnan(res["df"]) or se == 0:
        res.update({"p_value": np.nan, "ci95_low": np.nan, "ci95_high": np.nan})
        return res

    # p-value e IC95 via distribuição t
    # (também poderia usar scipy_stats.ttest_ind(..., equal_var=False), mas aqui usamos t manual + df)
    t = res["t_stat"]
    df = res["df"]
    p = 2.0 * float(scipy_stats.t.sf(abs(t), df))
    res["p_value"] = p

    tcrit = float(scipy_stats.t.ppf(1 - alpha / 2, df))
    res["ci95_low"] = float(res["mean_diff"] - tcrit * se)
    res["ci95_high"] = float(res["mean_diff"] + tcrit * se)
    return res


def run_welch_for_groups(
    results_root="results",
    output_folder="results/output",
    n_bins=10,
    use_per_seed=True,
    use_bins=True,
):
    """
    Executa Welch t-test (Rewards) para os grupos:
      2nd vs 2nd_2
      3rd vs 3rd_2
      4th vs 4th_2
      5th vs 5th_2

    - per_seed: amostra = média de reward por seed
    - per_bin:  amostra = série agregada (média entre seeds por episódio) depois agregada a cada n_bins episódios
    """
    pairs = [
        ("output_rew/2nd", "output_2/2nd"),
        ("output_rew/3rd", "output_2/3rd"),
        ("output_rew/4th", "output_2/4th"),
        ("output_rew/5th", "output_2/5th"),
    ]

    rows = []

    for a_name, b_name in pairs:
        a_path = os.path.join(results_root, a_name)
        b_path = os.path.join(results_root, b_name)

        if use_per_seed:
            a = sample_rewards_per_seed_mean(a_path)
            b = sample_rewards_per_seed_mean(b_path)
            res = welch_ttest(a, b)
            rows.append({
                "group": f"{a_name} vs {b_name}",
                "sample": "per_seed",
                **res
            })

        if use_bins:
            a_series = aggregate_mean_rewards_over_seeds(a_path)
            b_series = aggregate_mean_rewards_over_seeds(b_path)

            a_bins = aggregate_every_n(a_series, n=n_bins)
            b_bins = aggregate_every_n(b_series, n=n_bins)

            m = min(a_bins.size, b_bins.size)
            res = welch_ttest(a_bins[:m], b_bins[:m])
            rows.append({
                "group": f"{a_name} vs {b_name}",
                "sample": f"per_bin(n={n_bins})",
                **res
            })

    df = pd.DataFrame(rows)

    # Print tabela no terminal
    if df.empty:
        print("❌ Nenhum dado encontrado em results/<dataset>/(seed)/(data|profile)/nrewards.txt")
    else:
        cols = [
            "group", "sample",
            "n_A", "mean_A", "std_A",
            "n_B", "mean_B", "std_B",
            "mean_diff",
            "t_stat", "df", "p_value",
            "ci95_low", "ci95_high",
            "cohen_d"
        ]
        for c in cols:
            if c not in df.columns:
                df[c] = np.nan

        show = df[cols].copy()
        with pd.option_context("display.max_rows", None, "display.max_columns", None, "display.width", 180):
            print("\n=== Welch t-test (Rewards) por grupo ===")
            print(show.to_string(index=False, float_format=lambda x: f"{x:.6g}"))

    # Salva CSV
    os.makedirs(output_folder, exist_ok=True)
    out_csv = os.path.join(output_folder, "welch_rewards_groups.csv")
    df.to_csv(out_csv, index=False)
    print(f"\n📄 CSV salvo em: {out_csv}")

    return df


if __name__ == "__main__":
    # Ajuste aqui se quiser:
    RESULTS_ROOT = "results"
    OUTPUT_FOLDER = "results/output"
    N_BINS = 10

    run_welch_for_groups(
        results_root=RESULTS_ROOT,
        output_folder=OUTPUT_FOLDER,
        n_bins=N_BINS,
        use_per_seed=True,  # recomendado
        use_bins=True,      # usa os valores agregados (exemplo)
    )
