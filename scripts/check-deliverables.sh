#!/usr/bin/env bash
# Checks the non-code deliverables (brief §9). Exit code 0 only when all pass.
set -u
cd "$(dirname "$0")/.."
fails=0
ok()   { echo "  [ok]   $1"; }
bad()  { echo "  [FAIL] $1"; fails=$((fails+1)); }

echo "Deliverables check"

reg=docs/PATTERN_REGISTER.md
if [ -f "$reg" ]; then
  used=$(awk '/^## Used patterns/{f=1;next} /^## /{f=0} f' "$reg" | grep -cE '^\| *[0-9]+ *\|')
  c=$(awk '/^## Used patterns/{f=1;next} /^## /{f=0} f' "$reg" | grep -cE '\| *Creational *\|')
  s=$(awk '/^## Used patterns/{f=1;next} /^## /{f=0} f' "$reg" | grep -cE '\| *Structural *\|')
  b=$(awk '/^## Used patterns/{f=1;next} /^## /{f=0} f' "$reg" | grep -cE '\| *Behavioral *\|')
  rej=$(awk '/^## Rejected patterns/{f=1;next} /^## /{f=0} f' "$reg" | grep -cE '^\| *[A-Z][a-z]')
  rej=$((rej>0 ? rej-1 : 0))   # minus header row
  [ "$used" -ge 8 ] && ok "register: $used patterns (>= 8)" || bad "register: $used patterns (< 8)"
  [ "$c" -ge 2 ] && [ "$s" -ge 2 ] && [ "$b" -ge 3 ] \
    && ok "categories: C=$c S=$s B=$b" || bad "categories: C=$c S=$s B=$b (need 2/2/3)"
  [ "$rej" -ge 2 ] && ok "rejected patterns: $rej" || bad "rejected patterns: $rej (< 2)"
  grep -q '^## Combinations' "$reg" && ok "combination section present" || bad "no combination section"
else
  bad "missing $reg"
fi

adrs=$(ls docs/adr/[0-9][0-9][0-9][0-9]-*.md 2>/dev/null | grep -v 0000-template | wc -l)
[ "$adrs" -ge 5 ] && ok "ADRs: $adrs (>= 5)" || bad "ADRs: $adrs (< 5)"

refl=$(ls docs/reflection-*.md 2>/dev/null | grep -v TEMPLATE | wc -l)
[ "$refl" -ge 2 ] && ok "reflections: $refl" || bad "reflections: $refl (need one per student, at least 2)"

if grep -qE '_name_|_@handle_|TODO' README.md; then
  bad "README still contains placeholders (team table)"
else
  ok "README filled in"
fi
grep -q '```mermaid' README.md && ok "README has architecture diagram" || bad "README has no mermaid diagram"

if git rev-parse --git-dir >/dev/null 2>&1; then
  tags=$(git tag -l 'v*' | wc -l)
  [ "$tags" -ge 3 ] && ok "release tags: $tags" || bad "release tags: $tags (< 3)"
  git tag -l | grep -qx 'v1.0' && ok "tag v1.0 present" || bad "tag v1.0 missing"
else
  bad "not a git repository"
fi

echo
if [ "$fails" -eq 0 ]; then echo "All deliverables present."; else echo "$fails check(s) failed."; fi
exit "$fails"
