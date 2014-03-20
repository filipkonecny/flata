package acceleration;

public interface IAcceleration {

	public boolean isOctagon(IAccelerationInput input);
	/** 
	 * assumes: input is an octagon
	 */
	public IClosure closure(IAccelerationInput input);
	public IClosure closureOverapprox(IAccelerationInput input);
	public IClosure closureUnderapprox(IAccelerationInput input);
}
