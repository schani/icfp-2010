#$ -N icfp-dist
#$ -pe mpich 1
#$ -M mark.probst@gmail.com
#$ -l h_rt=03:30:00
#$ -ar 2026
#$ -t 1-400:1
#$ -cwd
#$ -V

./dist/runworker.sh
