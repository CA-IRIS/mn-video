#!/usr/bin/env python
# The video recorder module records the video for a single camera.

# Author: Timothy A. Johnson

import os, sys
from time import time, localtime, strftime
from video_file import VideoFile

class VideoRecorder():

	video_file = None
	stream_url = None
	camera = None
	ip = None
	stream_uri = '/mpeg4/1/media.amp'
	stream_protocol = 'rtsp'
	vlc_cmd = None
	last_index_time = None
	# The max time (in seconds) between video file indices.
	index_interval = 3
	container_type = 'ts'
	index_file = None

	def __init__(self, camera_number, ip, location):
		self.camera = camera_number
		self.ip = ip
		print 'Camera %s (%s)\t===>\t%s' % (
			self.camera, self.ip, location)
		self.video_file = location + '/' + self.camera + '.' + self.container_type
		self.index_file = VideoFile(location, self.camera)
		self.stream_url = (self.stream_protocol + "://" +
			self.ip + self.stream_uri)
		self.vlc_cmd = ('vlc ' + self.stream_url +
			' -I dummy --sout \'#std{mux=' + self.container_type +
			', access=file, dst=-}\'')

	def stop(self):
		'Stop recording a video stream'

	def get_stdout_stream(self):
		'Get the video stream from a sub-process stdout'
		fd_tuple = os.popen2(self.vlc_cmd)
		return fd_tuple[1]

	def record(self):
		'Record the video stream.'
		f_in = self.get_stdout_stream()
		f_out = open(self.video_file, 'w')
		buf_sz = 1024 * 1024
		file_sz = buf_sz * 1000 * 50
		os.ftruncate(f_out.fileno(), file_sz)
		f_out.seek(self.index_file.get_start())
		f_pos = f_out.tell()
		while(True):
			f_out.write( f_in.read(buf_sz) )
			f_pos = f_out.tell()
			if(f_pos > (file_sz - buf_sz)):
				print 'Rolling file...'
				f_out.seek(0)
			self.write_index(f_out)

	def write_index(self, v_file):
		#print 'File position: %s K' % (v_file.tell() / 1024)
		now = time()
		now_str = strftime('%Y-%m-%d %H:%M:%S', localtime(now))
		print now_str
		if(not self.last_index_time):
			self.last_index_time = now
		if((now - self.last_index_time) > self.index_interval):
			#print 'Index created at %s offset %s' % (now, v_file.tell())
			self.index_file.set_offset(now_str, v_file.tell())
			self.last_index_time = now

if __name__ == '__main__':
	vr = VideoRecorder('C629', '10.0.56.105', '/drive1')
	vr.record()
