all :
	@cd gnl && bash compile_to_lib.sh && rm -f *.o
	@echo "" > outfile
	@echo "" > messageFile.final
	@gcc  testclient.c -Lgnl -lgnl
	@echo "Compiled tester..."

clean: 
	@rm -rf *.o

fclean: clean
	@rm -rf a.out

re : fclean all