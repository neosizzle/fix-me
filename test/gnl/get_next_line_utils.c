#include "get_next_line.h"

//helpers
//calculates and gets the string length
size_t	ft_strlen(const char *s)
{
	size_t	res;

	res = 0;
	while (s[res])
		res++;
	return (res);
}

//joins s1 and s2 together
//allocates new memory
char	*ft_strcat(char *s1, char *s2)
{
	char	*res;
	size_t	i;
	size_t	j;

	res = (char *)malloc(sizeof(char) *(ft_strlen(s1) + ft_strlen(s2) + 1));
	if (!res)
		return (0);
	i = 0;
	j = 0;
	while (s1[i])
	{
		res[i + j] = s1[i];
		i++;
	}
	while (s2[j])
	{
		res[i + j] = s2[j];
		j++;
	}
	res[i + j] = '\0';
	return (res);
}

//finds a chr in a string, returns pointer to that char if found
char	*ft_strchr(const char *s, int c)
{
	size_t	i;
	size_t	len;

	i = 0;
	len = ft_strlen(s);
	while (i < len && s[i])
	{
		if (s[i] == c)
			return ((char *)(s + i));
		s++;
	}
	return (0);
}

//allocates mem and generates a substring based on the 
//satrting point and length of the og string
char	*ft_substr(const char *str, size_t start, size_t len)
{
	char	*res;
	size_t	i;

	if (!str)
		return (0);
	res = (char *)malloc(sizeof(char) * (len + 1));
	if (!res)
		return (0);
	i = 0;
	while (start < len)
		res[i++] = str[start++];
	res[i] = 0;
	return (res);
}

//allocates mem with size and fills it with zero
char	*ft_bzero(size_t size)
{
	char	*res;
	size_t	i;

	res = (char *)malloc(sizeof(char) * (size + 1));
	if (!res)
		return (0);
	i = 0;
	while (i < size + 1)
	{
		*(char *)(res + i) = 0;
		i++;
	}
	return (res);
}