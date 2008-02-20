#!/usr/bin/env python
# The video_file class provides convenience methods for finding 
# the start and end of a rolling video stream file.  It also
# provides access to the offset into the file for a given time.
# Additionally, it hides the implmentation of offset tracking.

# Author: Timothy A. Johnson

from time import time, localtime, strftime
import pgsql

class VideoFile():

	last_offset = None
	last_time = None
	wrap_time = None
	file_name = None
	camera_id = None
	connection = None
	db_name = 'nvr'
	offset_table = 'file_offset'
	cursor = None

	def __init__(self, file_location, camera_id):
		self.file_name = file_location + '/' + camera_id + '.ts'
		self.camera_id = camera_id
		self.connection = pgsql.connect(self.db_name)
		self.cursor = self.connection.cursor()
		create = ('create table ' + self.offset_table +
			' (file_name text not null, ' +
			'time timestamp not null, ' +
			'file_offset int4 not null, ' +
			'camera_id text not null)')
		#self.cursor.execute(create)
		self.init_wrap_time()
		self.last_offset = self.get_start()

	def get_start(self):
		'Get the offset into the file for the start of the stream'
		q = ('select file_offset, time from ' + self.offset_table +
			' where camera_id = \'' + self.camera_id + '\'' +
			' order by time desc')
		self.cursor.execute(q)
		record = self.cursor.fetchone()
		if not record:
			return 0		
		return record[0]

	def get_offset(self, timestamp):
		'Get the offset into the file for the given timestamp'
		q = ('select file_offset, time from ' +
			self.offset_table + ' where camera_id = \'' +
			self.camera_id + '\' and time >= \'' + timestamp +
			'\' order by time asc')
		self.cursor.execute(q)
		record = self.cursor.fetchall()[0]
		return record[0]

	def set_offset(self, current_time, current_offset):
		'Set the file offset for the given timestamp'
		i = ('insert into ' + self.offset_table +
			' (file_name,time,file_offset,camera_id) ' +
			'values' + ' (\'' +
			self.file_name + '\', \'' +
			current_time + '\', ' +
			str(current_offset) + ', \'' +
			self.camera_id + '\')')
		self.cursor.execute(i)
		self.connection.commit()
		if current_offset < self.last_offset:
			self.purge_by_time(self.wrap_time)
			self.wrap_time = current_time
		self.last_offset = current_offset
		self.purge_by_offset(current_offset)
		print '%s %s' % (self.wrap_time, current_time)

	def init_wrap_time(self):
		'Process all database records to find the time of the last file wrap.'
		q = ('select time, file_offset from ' +
			self.offset_table + ' where camera_id = \'' +
			self.camera_id + '\' order by time desc')
		self.cursor.execute(q)
		off = None
		records = self.cursor.fetchall()
		if not records:
			now = time()
			self.wrap_time = strftime('%Y-%m-%d %H:%M:%S',
				localtime(now))
			return
		for r in records:
			if not off:
				off = r[1]
				self.wrap_time = r[0]
			elif off > r[1]:
				self.wrap_time = r[0]
		print 'Wrap time = %s' % self.wrap_time

	def purge_by_offset(self, current_offset):
		'Delete obsolete offset records from the database.'
		print 'Purge by offset (wrap = %s)' % self.wrap_time
		q = ('delete from ' + self.offset_table +
			' where time < \'' + str(self.wrap_time) +
			'\' and file_offset <= ' + str(current_offset) +
			' and camera_id = \'' + self.camera_id + '\'')
		self.cursor.execute(q)
		self.connection.commit()

	def purge_by_time(self, time):
		'Delete obsolete offset records from the database.'
		q = ('delete from ' + self.offset_table +
			' where time < \'' + str(time) +
			'\' and camera_id = \'' + self.camera_id + '\'' )
		self.cursor.execute(q)
		self.connection.commit()

if __name__ == '__main__':
	fn = '/drive1/630.ts'
	vf = VideoFile(fn)
	timestamp = 1
	print 'At %s, the offset was %s' % (timestamp, vf.get_offset(timestamp))
	print 'The file, %s, starts at offset %s' % (fn, str(vf.get_start()))
