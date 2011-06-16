/*
 * COMSERVER Open Source Application Framework - http://www.thebitstream.com
 *
 * Copyright (c) 2009-2011 by Andy Shaules. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software
 * Foundation; either version 2.1 of the License, or (at your option) any later
 * version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along
 * with this library; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.red5.server.plugin.jni.objects.net;


import java.util.ArrayList;
import java.util.List;

import org.apache.mina.core.buffer.IoBuffer;
import org.red5.server.net.rtmp.event.IRTMPEvent;
import org.red5.server.net.rtmp.event.VideoData;
import org.red5.server.net.rtmp.message.Header;


/**
 * @author Andy Shaules (bowljoman@hotmail.com)
 *
 */
public class VideoFramer implements IGForce {
	
	/**
	 * To force the VideoFramer to send h264 codec info with every key frame.
	 * Default is to allow the ICYMarshal to send it just once. 
	 */
	public static boolean SEND_ALL_AVC_CONFIGURATION_PACKETS =false;
	
	private ICudaListener output;
	
//	private Object lock=new Object();
		
	private int _codecSetupLength;

	private boolean codecSent;

	private int _SPSLength;

	private int _PPSLength;

	private int[] _pCodecSetup;

	private int[] _pSPS;

	private int[] _pPPS;

	private int[] _pSEI;

	//private int _SEILength;


	


	private IRTMPEvent lastKey;



	private List<IRTMPEvent> slices=new ArrayList<IRTMPEvent>();
	

	
	public VideoFramer( ) {

	
		
		_codecSetupLength = 0;
		
		//output = outputSink;
	}

	public void reset() {
		
		slices.clear();
		lastKey=null;
		_pSPS = null;
		_pPPS = null;
		_codecSetupLength = 0;
		_pCodecSetup = null;
		_SPSLength = 0;
		_PPSLength = 0;
	}



	/**
	 * Take raw h264 and format it for flv.
	 * @param frame
	 * @param timecode
	 */
	public void pushAVCFrame(byte[] frame, int timecode) {
	//	System.out.println("pushAVCFrame :"+timecode);
		
		if(frame.length ==0 )
		{	
			
			return;
		}		
		
		for (int i = 0; i < frame.length-4; i++) {
			if (frame[i] == 0) {
				if (frame[i + 1] == 0) {
					if (frame[i + 2] == 0) {
						if (frame[i + 3] == 1) {
						
							i += 4;//Cursor past 0001.
							// Look for next 8_bit_zero marker.
							int size = this.findFrameEnd(frame, i);

							if (size == -1)//From i to end of segment.
								size = frame.length - i;
							else
								//Size is from start of segment to next 8_bit_zero marker.
								size = size - i;

							processNal(frame, i, size,timecode);
							//cue next '0'001 point 
							i += size-1;
						}
					}else{
						if(frame[i + 2]== 1){
							
						}
					}
				}
			}
		}
	}

	/**
	 * Returns point of '0'001' marker or -1.
	 * @param frame The NALU stream.
	 * @param offset The point to search from.
	 * @return The point before the next 0001 marker.
	 */
	private int findFrameEnd(byte[] frame, int offset) {
		
		for (int i = offset; i < frame.length - 3; i++) {
			if (frame[i] == 0) {
				if (frame[i + 1] == 0) {
					if (frame[i + 2] == 0) {
						if (frame[i + 3] == 1) {
							return i;
						}
					}
				}
			}
		}
		
		return -1;
	}

	private void processNal(byte[] frame, int i, int size, int timecode) {

		int type = readNalHeader((byte) frame[i]);
		
		switch (type) {//process routine
			case SPS://renew
				_pSPS = null;
				_SPSLength = size;
				_pSPS = new int[size];
				for (int k = 0; k < size; k++) {
					_pSPS[k] = frame[k + i];
				}
				break;

			case PPS://renew
				_pPPS = null;
				_PPSLength = size;
				_pPPS = new int[size];
				for (int k = 0; k < size; k++) {
					_pPPS[k] = frame[k + i];
				}
				break;
			case SEI:
				_pSEI = null;
				_pSEI = new int[size];
				for (int k = 0; k < size; k++) {
					_pSEI[k] = frame[k + i];
				}
				break;
		}
		
		switch (type) {//send routine
			case IDR:
			
				slices.clear();
				if (!codecSent  ){
				buildCodecSetup();//rebuild any changes
				sendAVCDecoderConfig(timecode);
				}else if(SEND_ALL_AVC_CONFIGURATION_PACKETS){
					buildCodecSetup();
					sendAVCDecoderConfig();
				}
			
				if (!codecSent)
					return;
					
				IoBuffer buffV = IoBuffer.allocate(size + 9);
				buffV.setAutoExpand(true);
				buffV.put((byte) 0x17);//packet tag// key // avc
				buffV.put((byte) 0x01);//vid tag
				//vid tag presentation off set
				buffV.put((byte) 0);
				buffV.put((byte) 0);
				buffV.put((byte) 0);
				//nal size
				buffV.put((byte) ((size >> 24)));
				buffV.put((byte) ((size >> 16)));
				buffV.put((byte) ((size >> 8)));
				buffV.put((byte) ((size)));
				//nal data
				for (int r = 0; r < size - 1; r++) {
					buffV.put((byte) frame[r + i]);
				}
				buffV.put((byte) 0);
				buffV.flip();
				buffV.position(0);
				IRTMPEvent videoIDR = new VideoData(buffV);
				videoIDR.setHeader(new Header());
				videoIDR.getHeader().setTimer(timecode);
				videoIDR.setTimestamp(timecode);
				lastKey=videoIDR;
				
				if(output!=null)
				output.dispatchEvent(lastKey);
	
				break;
			case CodedSlice:
				if (!codecSent){					
					return;
					}
				IoBuffer buffV2 = IoBuffer.allocate(size + 9);
				buffV2.setAutoExpand(true);
				buffV2.put((byte) 0x27);//packet tag//non key//avc
				buffV2.put((byte) 0x01);//vid tag
				//presentation off set
				buffV2.put((byte) 0x0);
				buffV2.put((byte) 0);
				buffV2.put((byte) 0);
				//nal size
				buffV2.put((byte) ((size >> 24)));
				buffV2.put((byte) ((size >> 16)));
				buffV2.put((byte) ((size >> 8)));
				buffV2.put((byte) ((size)));
				//nal data
				for (int r = 0; r < size - 1; r++) {
					buffV2.put((byte) frame[r + i]);
				}
				buffV2.put((byte) 0);
				buffV2.flip();
				buffV2.position(0);
				IRTMPEvent CodedSlice = new VideoData(buffV2);
				CodedSlice.setTimestamp(timecode);
				CodedSlice.setHeader(new Header());
				CodedSlice.getHeader().setTimer(timecode);
				slices.add(CodedSlice);
				if(output!=null)
				output.dispatchEvent(CodedSlice);
			
				break;
		}
	}

	/**
	 * Returns next read position.
	 * @param data
	 * @param position
	 * @return
	 */
	private int readNalHeader(byte bite) {
		int NALUnitType = (int) (bite & 0x1F);
		
		return NALUnitType;
	}
	public synchronized IRTMPEvent getLastKey(){
		
		return lastKey;
	}
	public synchronized List<IRTMPEvent> getLastSlices(){
		return slices;
	}
	private void sendAVCDecoderConfig() {
		
		if (_pCodecSetup == null)
			return;
		IoBuffer buffV = IoBuffer.allocate(_pCodecSetup.length);
		buffV.setAutoExpand(true);
		for (int p = 0; p < _pCodecSetup.length; p++)
			buffV.put((byte) _pCodecSetup[p]);
		buffV.flip();
		buffV.position(0);
		IRTMPEvent video = new VideoData(buffV);
		video.setHeader(new Header());
		
		if(output!=null)
		output.dispatchEvent(video);
		
		codecSent = true;

	}
	private void sendAVCDecoderConfig(int timecode) {
		
		if (_pCodecSetup == null)
			return;
		IoBuffer buffV = IoBuffer.allocate(_pCodecSetup.length);
		buffV.setAutoExpand(true);
		for (int p = 0; p < _pCodecSetup.length; p++)
			buffV.put((byte) _pCodecSetup[p]);
		buffV.flip();
		buffV.position(0);
		IRTMPEvent video = new VideoData(buffV);
		video.setTimestamp(timecode);
		video.setHeader(new Header());
		if(output!=null)
		output.dispatchEvent(video);
		
		codecSent = true;

	}	
	public boolean hasSetup(){
		return (_pCodecSetup != null);
	}
	public synchronized IRTMPEvent getAVCDecoderConfig() {
		if (_pCodecSetup == null)
			return null;
		IoBuffer buffV = IoBuffer.allocate(_pCodecSetup.length);
		buffV.setAutoExpand(true);
		for (int p = 0; p < _pCodecSetup.length; p++)
			buffV.put((byte) _pCodecSetup[p]);
		buffV.flip();
		buffV.position(0);
		IRTMPEvent video = new VideoData(buffV);
		video.setHeader(new Header());
	
		return video;
	}

	private void buildCodecSetup() {
	
		if (_pPPS == null && _pSPS == null) {
			return;
		}

		_codecSetupLength = 5 //header
				+ 8 //SPS header
				+ _SPSLength //the SPS itself
				+ 3 //PPS header
				+ _PPSLength; //the PPS itself

		_pCodecSetup = new int[_codecSetupLength];
		int cursor = 0;
		//header
		_pCodecSetup[cursor++] = 0x17; //0x10 - key frame; 0x07 - H264_CODEC_ID
		_pCodecSetup[cursor++] = 0; //0: AVC sequence header; 1: AVC NALU; 2: AVC end of sequence
		_pCodecSetup[cursor++] = 0; //CompositionTime
		_pCodecSetup[cursor++] = 0; //CompositionTime
		_pCodecSetup[cursor++] = 0; //CompositionTime
		//SPS
		_pCodecSetup[cursor++] = 1; //version
		_pCodecSetup[cursor++] = _pSPS[1]; //profile
		_pCodecSetup[cursor++] = _pSPS[2]; //profile compat
		_pCodecSetup[cursor++] = _pSPS[3]; //level
		
		_pCodecSetup[cursor++] = 0x3; //6 bits reserved (111111) + 2 bits nal size length - 1 (11)//Adobe does not set reserved bytes.
		_pCodecSetup[cursor++] = 0x1; //3 bits reserved (111) + 5 bits number of sps (00001)//Adobe does not set reserved bytes.
		
		//sps length.
		_pCodecSetup[cursor++] = ((_SPSLength >> 8) & 0xFF);
		_pCodecSetup[cursor++] = (_SPSLength & 0xFF);
		
		//int sizeS = _pCodecSetup[cursor - 2] << 8 | _pCodecSetup[cursor - 1];
	
		
		//copy _pSPS data;
		for (int k = 0; k < _SPSLength; k++) {
			_pCodecSetup[cursor++] = _pSPS[k];
		}
		//PPS
		//copy PPS data;
		_pCodecSetup[cursor++] = 1; //version
		//short to big endian.
		_pCodecSetup[cursor++] = ((_PPSLength >> 8) & 0x000000FF);
		_pCodecSetup[cursor++] = (_PPSLength & 0x000000FF);
		
		
		//int sizeP = _pCodecSetup[cursor - 2] << 8 | _pCodecSetup[cursor - 1];
	
		//copy _pPPS data;
		for (int k = 0; k < _PPSLength; k++) {
			_pCodecSetup[cursor++] = _pPPS[k];
		}

	}
	public ICudaListener getOutput() {
		return output;
	}

	public void setOutput(ICudaListener output) {
		this.output = output;
	}
	// nsv byte stream nal sequence
	// 6,7,8,5,2,2,2,... 
	// sei,sps,pps,idr,slice,slice,slice,..., sps,pps,idr,slice,slice,...,
	private final int CodedSlice = 1;
	//private final int DataPartitionA = 2;
	//private final int DataPartitionB = 3;
	//private final int DataPartitionC = 4;
	private final int IDR = 5;
	private final int SEI = 6;
	private final int SPS = 7;
	private final int PPS = 8;
	//private final int AUD = 9;
	///private String[] names = {"Undefined", "Coded Slice", "Partition A", "Partition B", "Partition C", "IDR", "SEI", "SPS", "PPS", "AUD" };

}
