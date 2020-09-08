import * as React from "react";
import PropTypes from 'prop-types';

import { TextInput, SelectInput } from 'react-admin';
import { Fragment } from 'react'

// import SelectField from '@material-ui/core/SelectField';
import MenuItem from '@material-ui/core/MenuItem';
import { useInput } from 'react-admin';

import { Field } from 'react-final-form';


const TestInput = (props) => {
    console.log("dump")
    console.log(props)
    return (
        <span>{props.record.type}</span>
    )
}


const RoleTypeInput = ({
    record,
    source,
}) => {
    console.log("select ros")
    console.log(record)
    const { input, meta: { touched, error } } = useInput({ source });
    console.log(input)
    return (

        <SelectInput source={source} choices={[
            { id: 'organization', name: 'organization' },
            { id: 'space', name: 'space' },
            { id: 'component', name: 'component' },
        ]} record={record} {...input} />

        // <SelectField
        //     floatingLabelText="Type"
        //     errorText={touched && error}
        //     {...input}
        // >
        //     <MenuItem value="organization" />
        //     <MenuItem value="space" />
        //     <MenuItem value="component" />
        //     <MenuItem value="resource" />
        // </SelectField>
    );
};

// const RoleInput = ({
//     label,
//     helperText,
//     onBlur,
//     onFocus,
//     onChange,
//     options,
//     resource,
//     record,
//     source,
//     validate,
//     parse,
//     format,
//     ...rest
// }) => {
const RoleInput = (props) => {
    console.log("in input")

    // const {
    //     id,
    //     input,
    //     isRequired,
    //     meta: { error, touched },
    // } = useInput({
    //     format,
    //     onBlur,
    //     onChange,
    //     onFocus,
    //     parse,
    //     resource,
    //     source,
    //     validate,
    //     ...rest,
    // });

    console.log(props)
    const {
        id,
        input,
        isRequired,
        meta: { error, touched },
    } = useInput({
        ...props,
    });
    // const { input, meta: { touched, error } } = useInput({ source });

    console.log(input)
    // const record = props.record
    // console.log(record)
    // console.log(props.record)
    return (
        <Fragment>
            <RoleTypeInput source="type" record={input} />

        </Fragment>
    );
};

// RoleInput.propTypes = {
//     label: PropTypes.string,
//     record: PropTypes.object,
//     source: PropTypes.object,
// };


// RoleInput.propTypes = {
//     className: PropTypes.string,
//     label: PropTypes.oneOfType([PropTypes.string, PropTypes.bool]),
//     options: PropTypes.object,
//     resource: PropTypes.string,
//     record: PropTypes.object,
//     source: PropTypes.string,
// };

// RoleInput.defaultProps = {
//     options: {},
// };

export default RoleInput;
