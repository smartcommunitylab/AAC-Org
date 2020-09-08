import * as React from "react";
import { Show, SimpleShowLayout } from 'react-admin';
import { TabbedShowLayout, Tab } from 'react-admin'
import { EditButton, ListButton, DeleteButton, TopToolbar } from 'react-admin';

import { TextField, ChipField, ArrayField, ReferenceField, SingleFieldList } from 'react-admin';
import { FunctionField } from 'react-admin'
import Chip from '@material-ui/core/Chip'
import TextArrayField from '../fields/TextArrayField';

import { useQueryWithStore, Loading, Error } from 'react-admin';
import RecordTitle from '../fields/RecordTitle'


// const Spaces = ({ record }) => {
//     const { data, loading, error } = useQueryWithStore({
//         type: 'getManyReference',
//         resource: 'spaces',
//         payload: {
//             'target': 'components',
//             'id': record.id
//         }
//     });


//     if (loading) return <Loading />;
//     if (error) {
//         console.log('got error' + error);
//         console.dir(error);
//         return null;
//     }
//     if (!data) return null;

//     console.log("got data");
//     console.dir(data);

//     return (
//         <>
//             {
//                 data.length === 0 ?
//                     <span>No spaces found</span>
//                     :
//                     data.map(item => <Chip label={item} key={item} />)
//             }
//         </>
//     )

// };

// Spaces.defaultProps = {
//     addLabel: true,
// };



const ShowActions = ({ basePath, data, resource }) => (
    <TopToolbar>
        <EditButton basePath={basePath} record={data} />
        <DeleteButton basePath={basePath}
            record={data}
            resource={resource}
            undoable={false}
            redirect="list"
        />
        <ListButton basePath={basePath} />
    </TopToolbar>
);


export const ComponentShow = ({ permissions, ...props }) => (
    <Show title={<RecordTitle resource="component" />} actions={<ShowActions />} {...props}>
        <SimpleShowLayout>
            <FunctionField addLabel={false} render={record => <h1>{record.id} </h1>} />
            <TabbedShowLayout>
                <Tab label="summary">
                    <TextField source="id" />
                    <TextField source="name" />
                    <ReferenceField label="Owner" source="owner" reference="members" linkType="show">
                        <TextField source="username" />
                    </ReferenceField >
                    <TextField source="path" />
                </Tab>
                <Tab label="roles">
                    <TextArrayField source="roles" />
                </Tab>
                <Tab label="spaces">
                    {/* <Spaces label="spaces" /> */}
                    {/* <TextArrayField source="spaces" /> */}
                    <ArrayField source="spaces" >
                        <SingleFieldList>
                            {/* <TextField source="id" /> */}
                            <ReferenceField source="id" reference="spaces" link="show">
                                <ChipField source="id" />
                            </ReferenceField>
                        </SingleFieldList>
                    </ArrayField>
                </Tab>
                {/* <Tab label="members">
                </Tab> */}
            </TabbedShowLayout>
        </SimpleShowLayout>
    </Show>
);

export default ComponentShow;