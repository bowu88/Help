package com.pinsheng.mode;

public class DangerousaAll {
	private int status;
	private Data[] data;

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public Data[] getData() {
		return data;
	}

	public void setData(Data[] data) {
		this.data = data;
	}

	public class Data {
		private String imei;
		private double jingdu;
		private double weidu;
		private String address;
		private long time;

		public double getWeidu() {
			return weidu;
		}

		public void setWeidu(double weidu) {
			this.weidu = weidu;
		}

		public String getAddress() {
			return address;
		}

		public void setAddress(String address) {
			this.address = address;
		}

		public String getImei() {
			return imei;
		}

		public void setImei(String imei) {
			this.imei = imei;
		}

		public double getJingdu() {
			return jingdu;
		}

		public void setJingdu(double jingdu) {
			this.jingdu = jingdu;
		}

		public long getTime() {
			return time;
		}

		public void setTime(long time) {
			this.time = time;
		}

	}

}
