#include "get_next_line.h"
#include <stdio.h>
#define BUFFER_SIZE 1024
//function to free a pointer and points it to null byte
static void	ft_freestr(char **str)
{
	if (str)
	{
		free(*str);
		*str = 0;
	}
}

//helpr func to allocate mem and duplicate a string
static char	*ft_strdup(const char *str)
{
	size_t	i;
	char	*res;

	i = 0;
	res = (char *) malloc(sizeof(char) * (ft_strlen(str) + 1));
	if (!res)
		return (0);
	while (str[i])
	{
		res[i] = str[i];
		i++;
	}
	res[i] = 0;
	return (res);
}

//this function reads from a fd
//sets the number of bytes read to a pointer
//return the number of bytes read
static int	read_buff(int fd, char **buff, int *bytes_read)
{
	int	res;

	res = read(fd, *buff, BUFFER_SIZE);
	*bytes_read = res;
	return (res);
}

//function to extract a string containing newline
//it will allocate the memory for the result
//the result will contain the string that ends with \n
//if its end of file, it will contain a null termed str
//besides trimming the result, the remainder of that trim
//will get stored in str for next call
//e.g 
//12345\n2234
//res = 12345\n
//str = 2234
static char	*extract_line(char **str)
{
	size_t	i;
	char	*res;
	char	*temp;

	i = 0;
	while ((*str)[i] && (*str)[i] != '\n')
		i++;
	if ((*str)[i])
	{
		res = ft_substr(*str, 0, i + 1);
		temp = ft_strdup(*str + i + 1);
		ft_freestr(str);
		if (temp[0] != '\0')
			*str = temp;
		else
			ft_freestr(&temp);
	}
	else
	{
		res = ft_strdup(*str);
		ft_freestr(str);
	}
	return (res);
}

//gnl main func
//the general idea is to:
//1. read the fd for buff size with while its not EOF
//2. check the buff size if got any new lines
//	if yes, stop reading and trim the buffer and return
//	if no, continue reading 
//3. return null for err handling or EOF reached
char	*get_next_line(int fd)
{
	static char	*open_fds[1024];
	char		*buff;
	char		*temp;
	int			bytes_read;

	if (fd < 0 || fd > 1024 || BUFFER_SIZE < 1)
		return (0);
	buff = (char *) malloc(sizeof(char) * (BUFFER_SIZE + 1));
	if (!buff)
		return (0);
	while (read_buff(fd, &buff, &bytes_read) > 0)
	{
		buff[bytes_read] = 0;
		if (!open_fds[fd])
			open_fds[fd] = ft_bzero(0);
		temp = ft_strcat(open_fds[fd], buff);
		ft_freestr(&open_fds[fd]);
		open_fds[fd] = temp;
		if (ft_strchr(buff, '\n'))
			break ;
	}
	ft_freestr(&buff);
	if (bytes_read < 0 || (bytes_read == 0 && !open_fds[fd]))
		return (NULL);
	return (extract_line(&open_fds[fd]));
}