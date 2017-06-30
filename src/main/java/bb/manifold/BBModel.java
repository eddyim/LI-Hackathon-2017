package bb.manifold;

import manifold.api.fs.IFile;
import manifold.api.sourceprod.AbstractSingleFileModel;

import java.util.Set;

public class BBModel extends AbstractSingleFileModel
{
    BBModel( String fqn, Set<IFile> files )
    {
        super( fqn, files );
    }
}
