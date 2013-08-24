package com.subgraph.orchid;

import java.util.List;
import java.util.concurrent.TimeoutException;

import com.subgraph.orchid.data.IPv4Address;
import com.subgraph.orchid.data.exitpolicy.ExitTarget;

/**
 * A Circuit represents a logical path through multiple ORs.  Circuits are described in
 * section 5 of tor-spec.txt.
 *
 */
public interface Circuit {
	enum CircuitType { CIRCUIT_EXIT, CIRCUIT_DIRECTORY, CIRCUIT_INTERNAL }
	
	/**
	 * Return <code>true</code> if the circuit is presently in the connected state or
	 * <code>false</code> otherwise.
	 * 
	 * @return Returns <code>true</code> if the circuit is presently connected, or 
	 *                 <code>false</code> otherwise.
	 */
	boolean isConnected();
	
	/**
	 * Returns the entry router <code>Connection</code> object of this Circuit.  Throws
	 * a TorException if the circuit is not currently open.
	 *  
	 * @return The Connection object for the network connection to the entry router of this 
	 *         circuit.
	 * @throws TorException If this circuit is not currently connected.
	 */
	Connection getConnection();
	
	/**
	 * Returns the curcuit id value for this circuit.
	 * 
	 * @return The circuit id value for this circuit.
	 */
	int getCircuitId();
	
	/**
	 * Open an anonymous connection to the directory service running on the
	 * final node in this circuit.
	 * 
	 * @return The status response returned by trying to open the stream.
	 */
	Stream openDirectoryStream(long timeout) throws InterruptedException, TimeoutException, StreamConnectFailedException;
	
	/**
	 * Open an exit stream from the final node in this circuit to the 
	 * specified target address and port.
	 * 
	 * @param address The network address of the exit target.
	 * @param port The port of the exit target.
	 * @return The status response returned by trying to open the stream.
	 */
	Stream openExitStream(IPv4Address address, int port, long timeout) throws InterruptedException, TimeoutException, StreamConnectFailedException;
	
	/**
	 * Open an exit stream from the final node in this circuit to the
	 * specified target hostname and port.
	 * 
	 * @param hostname The network hostname of the exit target.
	 * @param port The port of the exit target.
	 * @return The status response returned by trying to open the stream.
	 */
	Stream openExitStream(String hostname, int port, long timeout) throws InterruptedException, TimeoutException, StreamConnectFailedException;
	
	/**
	 * Create a new relay cell which is configured for delivery to the specified
	 * circuit <code>targetNode</code> with command value <code>relayCommand</code>
	 * and a stream id value of <code>streamId</code>.  The returned <code>RelayCell</code>
	 * can then be used to populate the payload of the cell before delivering it.
	 * 
	 * @param relayCommand The command value to send in the relay cell header.
	 * @param streamId The stream id value to send in the relay cell header.
	 * @param targetNode The target circuit node to encrypt this cell for.
	 * @return A newly created relay cell object.
	 */
	RelayCell createRelayCell(int relayCommand, int streamId, CircuitNode targetNode);
	
	/**
	 * Returns the next relay response cell received on this circuit.  If no response is
	 * received within <code>CIRCUIT_RELAY_RESPONSE_TIMEOUT</code> milliseconds, <code>null</code>
	 * is returned.
	 * 
	 * @return The next relay response cell received on this circuit or <code>null</code> if
	 *         a timeout is reached before the next relay cell arrives.
	 */
	RelayCell receiveRelayCell();
	
	/**
	 * Encrypt and deliver the relay cell <code>cell</code>.
	 * 
	 * @param cell The relay cell to deliver over this circuit.
	 */
	void sendRelayCell(RelayCell cell);
	
	/**
	 * Return the last node or 'hop' in this circuit.
	 * 
	 * @return The final 'hop' or node of this circuit.
	 */
	CircuitNode getFinalCircuitNode();

	
	/**
	 * Return true if the final node of this circuit is believed to be able to connect to
	 * the specified <code>ExitTarget</code>.  Returns false if the target destination is
	 * not permitted by the exit policy of the final node in this circuit or if the target
	 * has been previously recorded to have failed through this circuit.
	 * 
	 * @param target The exit destination.
	 * @return Return true if is likely that the final node of this circuit can connect to the specified exit target.
	 */
	boolean canHandleExitTo(ExitTarget target);
	boolean canHandleExitToPort(int port);
	/**
	 * Records the specified <code>ExitTarget</code> as a failed connection so that {@link #canHandleExitTo(ExitTarget)} will
	 * no longer return true for this exit destination.
	 * 
	 * @param target The <code>ExitTarget</code> to which a connection has failed through this circuit.
	 */
	public void recordFailedExitTarget(ExitTarget target);

	void destroyCircuit();

	void deliverRelayCell(Cell cell);

	void deliverControlCell(Cell cell);
	
	List<Stream> getActiveStreams();

	void markForClose();
	
	void cannibalizeTo(Router target);
	
	CircuitType getCircuitType();
	
	void appendNode(CircuitNode node);
}
