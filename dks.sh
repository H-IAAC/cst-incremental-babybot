docker stop rl_csr_container
docker rm rl_csr_container
docker build -t rl_csr_image .
#docker run -d   -v /home/ic-unicamp/git/phd/SpecialIssue_CSR_RL/rl_CSR:/root/rl_CSR   -p 5901:5901   -p 8080:8080   -e VNC_PW="numero42" -e VNC_PASSWORD="numero42"   --name rl_csr_container   rl_csr_image
#docker logs --details rl_csr_container
docker run -d   -v /home/ic-unicamp/git/phd/SpecialIssue_CSR_RL/rl_CSR:/root/rl_CSR   -p 5901:5901   -p 8080:8080   -e VNC_PW="numero42" -e VNC_PASSWORD="numero42"   --name rl_csr_container   rl_csr_image
