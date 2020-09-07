export const ORG_SELECT = 'ORG_SELECT';
export const orgSelect = (org) => ({
    type: ORG_SELECT,
    payload: org,
});

export default orgSelect;