import os
import sys

def renomear_arquivos(base_dir: str) -> None:
    """
    Varre base_dir recursivamente e renomeia arquivos .jpg
    conforme a regra:
      - considerar apenas os últimos 3 itens do nome separados por "_"
      - descartar itens que sejam números (token.isdigit() == True)
    """
    if not os.path.isdir(base_dir):
        print(f'Erro: "{base_dir}" não é um diretório válido.')
        return

    for root, dirs, files in os.walk(base_dir):
        for nome_arquivo in files:
            if not nome_arquivo.lower().endswith(".jpg"):
                continue

            caminho_antigo = os.path.join(root, nome_arquivo)
            nome_sem_ext, ext = os.path.splitext(nome_arquivo)

            partes = nome_sem_ext.split("_")
            if not partes:
                continue

            # Pega apenas os últimos 3 itens
            ultimas_tres = partes[-3:]

            # Mantém apenas itens que NÃO são puramente numéricos
            partes_filtradas = [p for p in ultimas_tres if not p.isdigit()]

            # Se nada sobrou, não renomeia (evita nome vazio)
            if not partes_filtradas:
                print(f"Sem partes válidas (não numéricas) em: {caminho_antigo} -> mantendo nome.")
                continue

            novo_nome_sem_ext = "_".join(partes_filtradas)
            novo_nome_arquivo = novo_nome_sem_ext + ext  # mesma extensão (.jpg)
            caminho_novo = os.path.join(root, novo_nome_arquivo)

            # Se o nome novo é igual ao antigo, não faz nada
            if caminho_novo == caminho_antigo:
                continue

            # Evita sobrescrever um arquivo que já exista
            if os.path.exists(caminho_novo):
                print(f"Já existe arquivo com nome destino, pulando:\n  {caminho_antigo}\n  -> {caminho_novo}")
                continue

            print(f"Renomeando:\n  {caminho_antigo}\n  -> {caminho_novo}")
            os.rename(caminho_antigo, caminho_novo)

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("Uso: python renomear_jpgs.py /caminho/para/a/pasta")
        sys.exit(1)

    pasta_base = sys.argv[1]
    renomear_arquivos(pasta_base)
