
# garante que não tem alterações soltas
if [ -n "$(git status --porcelain)" ]; then
  echo "Seu working tree não está limpo. Commit/stash antes."
  exit 1
fi

git fetch --all --prune
git switch tutorial
git pull --ff-only

current="tutorial"

for b in $(git for-each-ref refs/heads --format='%(refname:short)' | grep -v "^${current}$"); do
  echo "==> Atualizando branch: $b"
  git switch "$b"
  git merge "$current" || {
    echo
    echo "CONFLITO no branch $b."
    echo "Resolva os conflitos, depois rode:"
    echo "  git add -A"
    echo "  git commit"
    echo "  git push origin $b"
    echo "E então rode o script novamente para continuar nos próximos."
    exit 1
  }
  git push origin "$b"
done

git switch tutorial
echo "Concluído."
