#include<stdio.h>
#include<stdlib.h>
#include<string.h>
#include<sys/socket.h>
#include<arpa/inet.h>
#include<unistd.h>
#include <fcntl.h>

char	*get_next_line(int fd);

char* itoa(int val, int base){
	
	char *buf = malloc(32);
	
	int i = 30;
	
	for(; val && i ; --i, val /= base)
	
		buf[i] = "0123456789abcdef"[val % base];
	
	return &buf[i+1];
	
}

char	*ft_strjoin(char const *s1, char const *s2)
{
	size_t	len;
	size_t	i;
	char	*res;

	len = strlen(s1) + strlen(s2);
	res = (char *)malloc(len + 1);
	if (!res)
		return (0);
	i = 0;
	while (*s1)
		res[i++] = (*s1++);
	while (*s2)
		res[i++] = (*s2++);
	res[i] = 0;
	return (res);
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

static void generateTestMessages(int fd, char* id)
{
    char *message;
    char *initMsg;
    char *msgToCheck;
    char *checksum;
    char soh[2] = {1, 0};

    message = strdup("hellllooo\n");
    printf("writing.. %s\n", message);
    write(fd, message, strlen(message));
    free(message);

    initMsg = strdup("|test|1234|10=123\n");
    message = ft_strjoin(id, replaceChar(initMsg, '|', 1));
    printf("writing.. %s\n", message);
    write(fd, message, strlen(message));
    free(message);
    free(initMsg);

    initMsg = strdup("|test|gg|ez|10=");
    msgToCheck = ft_strjoin(id, replaceChar(initMsg, '|', 1));
    free(initMsg);
    msgToCheck = replaceChar(msgToCheck, 's', '|');
    checksum = checksumCalc(msgToCheck);
    initMsg = strdup(msgToCheck);
    message = ft_strjoin(initMsg, checksum);
    message = ft_strjoin(message, soh);
    message = ft_strjoin(message, "\n");
    printf("writing.. %s\n", message);
    write(fd, message, strlen(message));
    free(message);
    free(msgToCheck);
    free(initMsg);

    initMsg = strdup("|test|gg|ez|10=");
    msgToCheck = ft_strjoin(id, replaceChar(initMsg, '|', 1));
    checksum = checksumCalc(msgToCheck);
    free(initMsg);
    initMsg = strdup(msgToCheck);
    message = ft_strjoin(initMsg, checksum);
    message = ft_strjoin(message, soh);
    message = ft_strjoin(message, "\n");
    printf("writing.. %s\n", message);
    write(fd, message, strlen(message));
    free(message);
    free(msgToCheck);
    free(initMsg);

    initMsg = strdup("|instrument=gayhat|market=123|price=1234|isBuy=false|10=");
    msgToCheck = ft_strjoin(id, replaceChar(initMsg, '|', 1));
    checksum = checksumCalc(msgToCheck);
    free(initMsg);
    initMsg = strdup(msgToCheck);
    message = ft_strjoin(initMsg, checksum);
    message = ft_strjoin(message, soh);
    message = ft_strjoin(message, "\n");
    printf("writing.. %s\n", message);
    write(fd, message, strlen(message));
    free(message);
    free(msgToCheck);
    free(initMsg);

    initMsg = strdup("|instrument=gayhat|market=12a3|price=1234|isBuy=false|10=");
    msgToCheck = ft_strjoin(id, replaceChar(initMsg, '|', 1));
    checksum = checksumCalc(msgToCheck);
    free(initMsg);
    initMsg = strdup(msgToCheck);
    message = ft_strjoin(initMsg, checksum);
    message = ft_strjoin(message, soh);
    message = ft_strjoin(message, "\n");
    printf("writing.. %s\n", message);
    write(fd, message, strlen(message));
    free(message);
    free(msgToCheck);
    free(initMsg);
}

static void	ft_strcpy(char *dst, char *begin, char *end)
{
	while (begin < end)
		*(dst++) = *(begin++);
	*dst = 0;
}

static int	get_tw(char *s, char c)
{
	int		res;

	res = 0;
	while (*s)
	{
		if (!(*s == c))
		{
			res++;
			while (*s && !(*s == c))
				s++;
		}
		else
			s++;
	}
	return (res);
}

char	**ft_split(char const *s, char c)
{
	char	*start;
	char	**res;
	int		i;

	if (!s)
		return (0);
	res = (char **)malloc((sizeof(char *) * (get_tw((char *)s, c) + 1)));
	if (!res)
		return (0);
	i = 0;
	while (*s)
	{
		if (*s != c)
		{
			start = (char *)s;
			while (*s && *s != c)
				s++;
			res[i] = (char *)malloc((char *)s - start + 1);
			ft_strcpy(res[i++], start, (char *)s);
		}
		else
			s++;
	}
	res[i] = 0;
	return (res);
}

//Create a Socket for server communication
short SocketCreate(void)
{
    short hSocket;
    printf("Create the socket\n");
    hSocket = socket(AF_INET, SOCK_STREAM, 0);
    return hSocket;
}
//try to connect with server
int SocketConnect(int hSocket)
{
    int iRetval=-1;
    int ServerPort = 5000;
    struct sockaddr_in remote= {0};
    remote.sin_addr.s_addr = inet_addr("127.0.0.1"); //Local Host
    remote.sin_family = AF_INET;
    remote.sin_port = htons(ServerPort);
    iRetval = connect(hSocket,(struct sockaddr *)&remote,sizeof(struct sockaddr_in));
    return iRetval;
}
// Send the data to the server and set the timeout of 20 seconds
int SocketSend(int hSocket,char* Rqst,short lenRqst)
{
    int shortRetval = -1;
    struct timeval tv;
    tv.tv_sec = 20;  /* 20 Secs Timeout */
    tv.tv_usec = 0;
    if(setsockopt(hSocket,SOL_SOCKET,SO_SNDTIMEO,(char *)&tv,sizeof(tv)) < 0)
    {
        printf("Time Out\n");
        return -1;
    }
    shortRetval = send(hSocket, Rqst, lenRqst, 0);
    return shortRetval;
}
//receive the data from the server
int SocketReceive(int hSocket,char* Rsp,short RvcSize)
{
    int shortRetval = -1;
    struct timeval tv;
    tv.tv_sec = 20;  /* 20 Secs Timeout */
    tv.tv_usec = 0;
    if(setsockopt(hSocket, SOL_SOCKET, SO_RCVTIMEO,(char *)&tv,sizeof(tv)) < 0)
    {
        printf("Time Out\n");
        return -1;
    }
    shortRetval = recv(hSocket, Rsp, RvcSize, 0);
    return shortRetval;
}
//main driver program
int main(int argc, char *argv[])
{
    int hSocket, read_size;
    struct sockaddr_in server;
    char *SendToServer;
    char server_reply[200] = {0};
    //Create socket
    hSocket = SocketCreate();
    if(hSocket == -1)
    {
        printf("Could not create socket\n");
        return 1;
    }
    printf("Socket is created\n");
    //Connect to remote server
    int serv_sock;
    if (serv_sock = SocketConnect(hSocket) < 0)
    {
        perror("connect failed.\n");
        return 1;
    }
    printf("Sucessfully conected with server\n");

	// record brokerId using gnl and split
    char *line = get_next_line(hSocket);
    char **lineTokens = ft_split(line, ' ');

	// open message list
    int msgFd = open("messageFile.final", O_WRONLY);

	// open outputfile
    int outFd = open("outfile", O_WRONLY);

    if (msgFd < 0 || outFd < 0)
    {
        perror("open or read");
        return 1;
    }

    // generate test messages
    generateTestMessages(msgFd, replaceChar(lineTokens[7], '\n', 0));
    close(msgFd);

    msgFd = open("messageFile.final", O_RDONLY);
    while (SendToServer = get_next_line(msgFd))
	{
		//Send data to the server
		SocketSend(hSocket, SendToServer, strlen(SendToServer));

		// write test number to output file
        write(outFd, "\n\nnewtest\n", 10);

		//Received the data from the server
		read_size = SocketReceive(hSocket, server_reply, 200);
		// write reply to output file
        int written = write(outFd, server_reply, strlen(server_reply));
        if (written < 0)
            perror("write to outfile");
        bzero(server_reply, strlen(server_reply));
        bzero(SendToServer, strlen(SendToServer));
	}

	// close in out files
    close(msgFd);
	close(outFd);
    close(hSocket);

    shutdown(hSocket,0);
    shutdown(hSocket,1);
    shutdown(hSocket,2);
    printf("success\n");
    return 0;
}