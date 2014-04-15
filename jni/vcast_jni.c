#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <fcntl.h>
#include <sys/stat.h>
#include <microhttpd.h>
#include <jni.h>
#include <android/log.h>


#define TAG "VCastServer"
#define LOGV(...) __android_log_print(ANDROID_LOG_VERBOSE, TAG, __VA_ARGS__)

struct MHD_Daemon *vcast_daemon;
char *range_value;

static int print_out_key(void *cls, enum MHD_ValueKind kind, const char *key, const char *value)
{
  if (strcmp(key, MHD_HTTP_HEADER_RANGE) == 0)
    range_value = value;
  return MHD_YES;
}

static int ahc_echo(void *cls,
    struct MHD_Connection *connection,
    const char *url,
    const char *method,
    const char *version,
    const char *upload_data,
    size_t *upload_data_size,
    void **ptr) {

  int ret;
  int fd;
  struct stat statbuff;

  if ((-1 == (fd = open(url, O_RDONLY))) || (0 != fstat(fd, &statbuff))) {
    if (fd != -1) close(fd);
    return MHD_NO;
  }

  if (range_value) {
    range_value = NULL;
  }

  MHD_get_connection_values(connection, MHD_HEADER_KIND, print_out_key, NULL);

	char *result = NULL;
	char content_range[256];
	int64_t offset = 0;

  if (range_value) {
    result = strtok(range_value, "=");
    if(result != NULL) {
      result = strtok(NULL, "=");
      offset = atof(result);
    }
  }
	if (!result)
		result = "0-";

  snprintf(content_range, sizeof(content_range), "bytes %s/%lld", result, statbuff.st_size);

  struct MHD_Response *response;
  response = MHD_create_response_from_fd_at_offset(statbuff.st_size, fd, offset);
  MHD_add_response_header(response, MHD_HTTP_HEADER_SERVER, "VCast");
  MHD_add_response_header(response, MHD_HTTP_HEADER_CONTENT_TYPE, "video/mp4");
  MHD_add_response_header(response, MHD_HTTP_HEADER_ACCEPT_RANGES, "bytes");
	MHD_add_response_header(response, MHD_HTTP_HEADER_CONTENT_RANGE, content_range);
	ret = MHD_queue_response(connection, MHD_HTTP_PARTIAL_CONTENT, response);
  MHD_destroy_response(response);
  return ret;
}

jint Java_com_example_vaircast_MainActivity_startServer(JNIEnv* env, jobject thiz, jint port){
  if (vcast_daemon != NULL) {
    MHD_stop_daemon(vcast_daemon);
  }

  vcast_daemon = MHD_start_daemon(MHD_USE_THREAD_PER_CONNECTION,
      port,
      NULL,
      NULL,
      &ahc_echo,
      NULL,
      MHD_OPTION_END);
  if (vcast_daemon == NULL)
    return 0;
  return 1;
}

void Java_com_example_vaircast_MainActivity_stopServer(JNIEnv* env, jobject thiz) {
  if (vcast_daemon != NULL) {
    MHD_stop_daemon(vcast_daemon);
    vcast_daemon = NULL;
  }
}

