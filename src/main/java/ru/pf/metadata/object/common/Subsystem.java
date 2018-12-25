package ru.pf.metadata.object.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import ru.pf.metadata.object.AbstractObject;
import ru.pf.metadata.reader.ObjectReader;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * @author a.kakushin
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class Subsystem extends AbstractObject<Subsystem> {

    private Set<String> items;
    private Set<Subsystem> child;

    public Subsystem(Path path) {
        super(path);
        this.child = new LinkedHashSet<>();
        this.items = new LinkedHashSet<>();
    }

    public Set<String> getItems() {
        return items;
    }

    @JsonIgnore
    public Set<String> getAllItems() {
        Set<String> result = new HashSet<>();
        result.addAll(getItems());
        for (Subsystem subsystem : getChild()) {
            result.addAll(subsystem.getAllItems());
        }
        return result;
    }

    public Set<Subsystem> getChild() {
        return child;
    }

    @Override
    public void parse() {
        Path fileXml = super.getFile().getParent().resolve(super.getFile());
        if (Files.exists(fileXml)) {
            ObjectReader objReader = new ObjectReader(fileXml);
            objReader.fillCommonField(this);

            // Чтение подчиненых подсистем
            List<String> childXml = objReader.readChild("/MetaDataObject/Subsystem/ChildObjects/Subsystem");
            for (String item : childXml) {
                Path childPath = fileXml.getParent()
                        .resolve(this.getShortName(fileXml))
                        .resolve("Subsystems")
                        .resolve(item + ".xml");

                Subsystem children = new Subsystem(childPath);
                children.parse();

                this.child.add(children);
            }

            // Чтение объектов, включенных в подсистему
            List<String> itemsXml = objReader.readChild("/MetaDataObject/Subsystem/Properties/Content/Item");
            for (String item : itemsXml) {
                this.items.add(item);
            }
        }
    }
}
