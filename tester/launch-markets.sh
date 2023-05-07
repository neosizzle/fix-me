echo "Please launch your router now.... (bash launch-router.sh)"
read lol

# launch 4 markets
echo "(tester/outputs/market1)" ; bash launch-market.sh > tester/outputs/market1 &
echo "(tester/outputs/market2)" ; bash launch-market.sh > tester/outputs/market2 &
echo "(tester/outputs/market3)" ; bash launch-market.sh > tester/outputs/market3 &
echo "(tester/outputs/market4)" ; bash launch-market.sh > tester/outputs/market4 &
