public class FileNode extends java.io.File {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public FileNode(String directory) {
        super(directory);
    }

    public FileNode(FileNode parent, String child) {
        super(parent, child);
    }

    @Override
    public String toString() {
        return getName();
    }
}