export interface good {
    id?: number,
    name?: string,
    price?: string,
}

export interface apiGood extends Omit<good, 'price'> {
    price?: number,
}
