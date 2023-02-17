#include <unistd.h>
#include <stdlib.h>
#include <string.h>
#include <stdio.h>

char* itoa(int val, int base){
	
	char *buf = malloc(32);
	
	int i = 30;
	
	for(; val && i ; --i, val /= base)
	
		buf[i] = "0123456789abcdef"[val % base];
	
	return &buf[i+1];
	
}

static char * replaceChar(char *str, char find, char replaced)
{
    int i = -1;
    while (str[++i])
    {
        if (str[i] == find)
            str[i] = replaced;
    }
    return str;
}

static char *checksumCalc(char *msg)
{
    char *res;
    int value;
    int i;

    value = 0;
    i = -1;
    res = malloc(69);
    if (strlen(msg) < 4)
    {
        res = itoa(0, 10);
    return res;
    }
    while (msg[++i] && !(msg[i] == '1' && msg[i + 1] == '0' && msg[i + 2] == '='))
    {
        char c = msg[i];
        // printf("calc %c\n", c);
        value += c;
    }
    value %= 256;
    res = itoa(value, 10);
    // printf("value %d\n", value);
    return res;
}

// repalces pipes woth soh and generates checksum
int main(int argc, char *argv[])
{
	if (argc != 2) return 0;

	char *msg = argv[1];

	char *checksum = checksumCalc(replaceChar(msg, '|', 1));
	printf("%s\n", checksum);
	return 0;
}
