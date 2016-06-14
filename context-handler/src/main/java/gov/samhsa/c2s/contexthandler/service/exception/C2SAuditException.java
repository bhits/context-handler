package gov.samhsa.c2s.contexthandler.service.exception;



public class C2SAuditException extends Exception {

	private static final long serialVersionUID = 160567101541197888L;

	public C2SAuditException() {
		super();
	}

	public C2SAuditException(String arg0) {
		super(arg0);
	}

	public C2SAuditException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public C2SAuditException(Throwable arg0) {
		super(arg0);
	}
}
