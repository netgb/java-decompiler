package net.jsmith.java.decomp.workspace.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import net.jsmith.java.decomp.workspace.Container;
import net.jsmith.java.decomp.workspace.Metadata;
import net.jsmith.java.decomp.workspace.Type;

public class TypeImpl implements Type {

	private final AbstractContainer owningContainer;
	private final MetadataImpl metadata;
	
	public TypeImpl( AbstractContainer container, MetadataImpl metadata ) {
		this.owningContainer = Objects.requireNonNull( container, "container" );
		this.metadata = Objects.requireNonNull( metadata, "metadata" );
	}
	
	@Override
	public Container getContainer( ) {
		return this.owningContainer;
	}

	@Override
	public Metadata getMetadata( ) {
		return this.metadata;
	}

	@Override
	public CompletableFuture< InputStream > getInputStream( ) {
		CompletableFuture< InputStream > promise = new CompletableFuture< >( );
		
		this.owningContainer.incReference( );
		this.owningContainer.getWorkspace( ).schedule( ( ) -> {
			try {
				InputStream is = this.owningContainer.getInputStream( this.metadata.getFullName( ) );
				promise.complete( new ContainerInputStream( this.owningContainer, is ) );
			}
			catch( IOException ioe ) {
				promise.completeExceptionally( ioe );
			}
		} );
		
		
		return promise;
	}

}