import * as React from "react";
import PropTypes from 'prop-types';
import Chip from '@material-ui/core/Chip';

//members icon
import PeopleIcon from '@material-ui/icons/People';
//spaces icon
import DnsIcon from '@material-ui/icons/Dns';
//components icon
import AppsIcon from '@material-ui/icons/Apps';
//org icon
import AccountBalanceIcon from '@material-ui/icons/AccountBalance';


const extractIcon = (record = {}) => {
    switch (record.type) {
        case 'organization': return <AccountBalanceIcon />;
        case 'space': return <DnsIcon />;
        case 'component': return <AppsIcon />;
        default: return false;
    }
}

const extractLabel = (record = {}) => {
    switch (record.type) {
        case 'organization': return record.role;
        case 'space': return record.space + ':' + record.role;
        case 'component': return record.component + ':' + record.space + ':' + record.role;
        case 'resource': return record.role;
        default: return '';
    }
}

const RoleFieldChip = (({ classes, record = {} }) => (
    //     record.type === 'organization' ? (<span><strong>{record['type']}:{record['role']}</span>) : null;
    // record.type === 'space' ? () : null;
    // record.type === 'component' ? () : null;
    // <span>
    //     <strong>{record['type']}</strong> {record['label']}
    //      {
    //         {
    //             'organization': record.role,
    //             'space': record.space + ':' + record.role,
    //             'component': record.component + ':' + record.role,
    //             'resource': record.role

    //         }[record.type]
    //      }
    // </span>


    //record.label = extractLabel(record);


    <span><Chip icon={extractIcon(record)} label={extractLabel(record)} /></span>
));

RoleFieldChip.propTypes = {
    label: PropTypes.string,
    record: PropTypes.object,
};

export default RoleFieldChip;
