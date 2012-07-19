package evymind.vapor.server.session;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.Enumeration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import evyframework.common.io.utils.IOUtils;

public class HashedSession extends AbstractSession {
	
	private static final Logger log = LoggerFactory.getLogger(HashedSession.class);

	private final HashSessionManager hashSessionManager;

	/**
	 * Whether the session has been saved because it has been deemed idle; in
	 * which case its attribute map will have been saved and cleared.
	 */
	private transient boolean idled = false;

	/**
	 * Whether there has already been an attempt to save this session which has
	 * failed. If there has, there will be no more save attempts for this
	 * session. This is to stop the logs being flooded with errors due to
	 * serialization failures that are most likely caused by user data stored in
	 * the session that is not serializable.
	 */
	private transient boolean saveFailed = false;

	/* ------------------------------------------------------------- */
	protected HashedSession(HashSessionManager hashSessionManager, String id) {
		super(hashSessionManager, id);
		this.hashSessionManager = hashSessionManager;
	}

	/* ------------------------------------------------------------- */
	protected HashedSession(HashSessionManager hashSessionManager, long created, long accessed, String id) {
		super(hashSessionManager, created, accessed, id);
		this.hashSessionManager = hashSessionManager;
	}

	/* ------------------------------------------------------------- */
	protected void checkValid() {
		if (this.hashSessionManager.idleSavePeriodMs != 0)
			deIdle();
		super.checkValid();
	}

	/* ------------------------------------------------------------- */
	@Override
	public void setMaxInactiveInterval(int secs) {
		super.setMaxInactiveInterval(secs);
		if (getMaxInactiveInterval() > 0
				&& (getMaxInactiveInterval() * 1000L / 10) < this.hashSessionManager.scavengePeriodMs)
			this.hashSessionManager.setScavengePeriod((secs + 9) / 10);
	}


	@Override
	protected void doInvalidate() throws IllegalStateException {
		super.doInvalidate();

		// Remove from the disk
		if (this.hashSessionManager.storeDir != null && getId() != null) {
			String id = getId();
			File f = new File(this.hashSessionManager.storeDir, id);
			f.delete();
		}
	}


	synchronized void save(boolean reactivate) {
		// Only idle the session if not already idled and no previous save/idle
		// has failed
		if (!isIdled() && !this.saveFailed) {
			
			log.debug("Saving " + super.getId() + " " + reactivate);

			File file = null;
			FileOutputStream fos = null;

			try {
				file = new File(this.hashSessionManager.storeDir, super.getId());

				if (file.exists())
					file.delete();
				file.createNewFile();
				fos = new FileOutputStream(file);
				willPassivate();
				save(fos);
				if (reactivate)
					didActivate();
				else
					clearAttributes();
			} catch (Exception e) {
				saveFailed(); // We won't try again for this session

				log.warn("Problem saving session " + super.getId(), e);

				if (fos != null) {
					// Must not leave the file open if the saving failed
					IOUtils.close(fos);
					// No point keeping the file if we didn't save the whole
					// session
					file.delete();
					this.idled = false; // assume problem was before
										// this.values.clear();
				}
			}
		}
	}


	public synchronized void save(OutputStream os) throws IOException {
		DataOutputStream out = new DataOutputStream(os);
		out.writeUTF(getId());
		out.writeLong(getCreationTime());
		out.writeLong(getAccessed());

		/*
		 * Don't write these out, as they don't make sense to store because they
		 * either they cannot be true or their value will be restored in the
		 * Session constructor.
		 */
		// out.writeBoolean(this.invalid);
		// out.writeBoolean(this.doInvalidate);
		// out.writeLong(this.maxIdleMs);
		// out.writeBoolean( this.newSession);
		out.writeInt(getRequests());
		out.writeInt(getAttributes());
		ObjectOutputStream oos = new ObjectOutputStream(out);
		Enumeration<String> e = getAttributeNames();
		while (e.hasMoreElements()) {
			String key = e.nextElement();
			oos.writeUTF(key);
			oos.writeObject(doGet(key));
		}
		oos.close();
	}


	public synchronized void deIdle() {
		if (isIdled()) {
			// Access now to prevent race with idling period
			access(System.currentTimeMillis());

			log.debug("Deidling " + super.getId());

			FileInputStream fis = null;

			try {
				File file = new File(this.hashSessionManager.storeDir, super.getId());
				if (!file.exists() || !file.canRead())
					throw new FileNotFoundException(file.getName());

				fis = new FileInputStream(file);
				this.idled = false;
				this.hashSessionManager.restoreSession(fis, this);

				didActivate();

				// If we are doing period saves, then there is no point deleting
				// at this point
				if (this.hashSessionManager.savePeriodMs == 0)
					file.delete();
			} catch (Exception e) {
				log.warn("Problem deidling session " + super.getId(), e);
				IOUtils.close(fis);
				invalidate();
			}
		}
	}


	/**
	 * Idle the session to reduce session memory footprint.
	 * 
	 * The session is idled by persisting it, then clearing the session values
	 * attribute map and finally setting it to an idled state.
	 */
	public synchronized void idle() {
		save(false);
	}


	public synchronized boolean isIdled() {
		return this.idled;
	}


	public synchronized boolean isSaveFailed() {
		return this.saveFailed;
	}


	public synchronized void saveFailed() {
		this.saveFailed = true;
	}

}
